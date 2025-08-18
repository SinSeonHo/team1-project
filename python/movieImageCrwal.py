import os
import requests
import mysql.connector
import time
import uuid
import re
import shutil

# 1. 이미지 저장 경로 (리눅스 운영용)
BASE_PATH = "/var/lib/ott/static/images/movieimages"
STATIC_PATH = "/var/lib/ott/static"
os.makedirs(BASE_PATH, exist_ok=True)

# 2. MySQL DB 연결 (환경변수 사용)
conn = mysql.connector.connect(
    host=os.getenv("DB_HOST", "127.0.0.1"),
    port=int(os.getenv("DB_PORT", 3306)),
    user=os.getenv("DB_USER", "ott_test"),
    password=os.getenv("DB_PASS", "12345"),
    database=os.getenv("DB_NAME", "ott_test"),
    charset="utf8mb4",
    use_unicode=True,
)
cursor = conn.cursor()

# 3. TMDb API 정보
API_KEY = "cfa721d14c0aa4e4b28b3f26e81369f9"
SEARCH_API_URL = "https://api.themoviedb.org/3/search/movie"
DETAIL_API_URL = "https://api.themoviedb.org/3/movie/{movie_id}/images"
IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

# 4. 이미지가 없는 영화만 조회
cursor.execute("SELECT mid, title FROM movie WHERE image_id IS NULL")
movies = cursor.fetchall()


# 5. 이미지 다운로드 함수
def download_image(url, path):
    try:
        response = requests.get(url, stream=True, timeout=10)
        if response.status_code == 200:
            with open(path, "wb") as f:
                shutil.copyfileobj(response.raw, f)
            return True
        else:
            print(
                f"[ERROR] 이미지 다운로드 실패 (status code={response.status_code}): {url}"
            )
            return False
    except Exception as e:
        print(f"[ERROR] 이미지 다운로드 중 예외 발생: {e}")
        return False


try:
    for mid, title in movies:
        print(f"\n[{title}] 이미지 검색 시작")

        poster_url = None
        movie_id = None

        # 1) TMDb API로 포스터 검색
        try:
            params = {"query": title, "language": "ko-KR", "api_key": API_KEY}
            response = requests.get(SEARCH_API_URL, params=params, timeout=10)
            if response.status_code == 200:
                data = response.json()
                if "results" in data and data["results"]:
                    matched = data["results"][0]
                    movie_id = matched.get("id")
                    poster_path = matched.get("poster_path")
                    if poster_path:
                        poster_url = IMAGE_BASE_URL + poster_path
                        print(f"[{title}] → TMDb 포스터 찾음: {poster_url}")
        except Exception as e:
            print(f"[{title}] → TMDb 오류: {e}")

        if not poster_url:
            print(f"[{title}] → 포스터 없음 → 건너뜀")
            continue

        try:
            # 파일명 및 경로
            unique_id = str(uuid.uuid4())
            safe_title = re.sub(r"[^a-zA-Z0-9_-]", "", title.strip())
            ext = poster_url.split(".")[-1].split("?")[0]
            if len(ext) > 5 or "/" in ext:
                ext = "jpg"
            file_name = (
                f"{unique_id}_{safe_title}.{ext}"
                if safe_title
                else f"{unique_id}.{ext}"
            )
            full_path = os.path.join(BASE_PATH, file_name)

            # 이미지 저장
            if download_image(poster_url, full_path):
                print(f"[{title}] → 이미지 저장 완료: {file_name}")
            else:
                print(f"[{title}] → 이미지 저장 실패: {file_name}")
                continue

            # DB에 저장
            cursor.execute(
                "INSERT INTO image (uuid, img_name, path) VALUES (%s, %s, %s)",
                (unique_id, file_name, "images/movieimages/" + file_name),
            )
            conn.commit()

            image_id = cursor.lastrowid
            cursor.execute(
                "UPDATE movie SET image_id = %s WHERE mid = %s", (image_id, mid)
            )

            # 스틸컷 처리 (최대 10장)
            if movie_id:
                try:
                    detail_url = DETAIL_API_URL.format(movie_id=movie_id)
                    detail_res = requests.get(
                        detail_url, params={"api_key": API_KEY}, timeout=10
                    )
                    if detail_res.status_code == 200:
                        image_data = detail_res.json()
                        backdrops = image_data.get("backdrops", [])[:10]
                        for b in backdrops:
                            path = b.get("file_path")
                            if path:
                                screenshot_url = IMAGE_BASE_URL + path
                                cursor.execute(
                                    "INSERT INTO image_screenshots (image_id, screenshot_url) VALUES (%s, %s)",
                                    (image_id, screenshot_url),
                                )
                                print(f"[{title}] → 스틸컷 저장: {screenshot_url}")
                                time.sleep(0.1)
                except Exception as e:
                    print(f"[{title}] → 스틸컷 처리 오류: {e}")

            conn.commit()
            print(f"[{title}] → DB 저장 완료")

        except Exception as e:
            print(f"[ERROR] {title} 처리 중 오류 발생: {e}")
            conn.rollback()

finally:
    cursor.close()
    conn.close()
    print("\n[모든 작업 완료]")
