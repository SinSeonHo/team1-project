import os
import requests
import mysql.connector
import time
import uuid
import re
from selenium import webdriver
from selenium.webdriver.common.by import By
import shutil

# 1. 이미지 저장 경로 설정
BASE_PATH = r"C:/upload/images/movieimages"  # 저장할 실제 이미지 경로
STATIC_PATH = os.path.abspath(
    "../src/main/resources/static"
)  # static 기준 상대경로 산출용
os.makedirs(BASE_PATH, exist_ok=True)  # 디렉토리 없으면 생성

# 2. MySQL DB 연결 설정
conn = mysql.connector.connect(
    host="localhost",
    port=3306,
    user="ott_test",
    password="12345",
    database="ott_test",
    charset="utf8mb4",
)
cursor = conn.cursor()

# 3. TMDb API 정보
API_KEY = "cfa721d14c0aa4e4b28b3f26e81369f9"
SEARCH_API_URL = "https://api.themoviedb.org/3/search/movie"
DETAIL_API_URL = "https://api.themoviedb.org/3/movie/{movie_id}/images"
IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

# 4. 셀레니움 (크롬) 설정 - headless 모드
options = webdriver.ChromeOptions()
options.add_argument("--headless")  # 창 없이 실행
driver = webdriver.Chrome(options=options)

# 5. 이미지가 없는 영화만 조회
cursor.execute("SELECT mid, title FROM movie WHERE image_id IS NULL")
movies = cursor.fetchall()


# 6. 이미지 다운로드 함수
def download_image(url, path):
    try:
        response = requests.get(url, stream=True, verify=False)
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
        movie_id = None  # TMDb 고유 ID 저장용
        backdrops = []

        # 1) TMDb API를 통해 영화 포스터 검색
        try:
            print(f"[{title}] → TMDb 검색 시도 중...")
            params = {"query": title, "language": "ko-KR", "api_key": API_KEY}
            response = requests.get(SEARCH_API_URL, params=params)
            if response.status_code == 200:
                data = response.json()
                if "results" in data and data["results"]:
                    matched = next(
                        (
                            r
                            for r in data["results"]
                            if r.get("title", "").strip().lower()
                            == title.strip().lower()
                        ),
                        data["results"][0],
                    )
                    movie_id = matched.get("id")
                    poster_path = matched.get("poster_path")
                    if poster_path:
                        poster_url = IMAGE_BASE_URL + poster_path
                        print(f"[{title}] → TMDb 포스터 찾음: {poster_url}")
                    else:
                        print(f"[{title}] → TMDb에 포스터 없음")
                else:
                    print(f"[{title}] → TMDb 검색 결과 없음")
            else:
                print(
                    f"[{title}] → TMDb 요청 실패 (status code: {response.status_code})"
                )
        except Exception as e:
            print(f"[{title}] → TMDb 오류: {e}")

        # 2) TMDb 실패 시 네이버 이미지 검색 시도
        if not poster_url:
            try:
                print(f"[{title}] → TMDb 실패 → 네이버 이미지 검색 시도")
                search_url = (
                    f"https://search.naver.com/search.naver?query={title}+포스터"
                )
                driver.get(search_url)
                time.sleep(2)

                imgs = driver.find_elements(
                    By.CSS_SELECTOR, ".area_card._image_base_poster ul li a img"
                )
                for img in imgs:
                    poster_url = img.get_attribute("src") or img.get_attribute(
                        "data-src"
                    )
                    if poster_url and poster_url.strip():
                        print(f"[{title}] → 네이버 이미지 찾음: {poster_url}")
                        break

                if not poster_url:
                    print(f"[{title}] → 네이버에서도 유효한 이미지 없음")
            except Exception as e:
                print(f"[{title}] → 네이버 이미지 검색 오류: {e}")

        # 3) 최종적으로 포스터 없으면 해당 영화 건너뜀
        if not poster_url:
            print(f"[{title}] → 최종적으로 포스터 없음 → 건너뜀")
            continue

        try:
            # 4) 파일명 및 경로 안전하게 구성
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

            # 5) 이미지 저장
            if download_image(poster_url, full_path):
                print(f"[{title}] → 이미지 저장 완료: {file_name}")
            else:
                print(f"[{title}] → 이미지 저장 실패: {file_name}")
                continue

            # 6) 상대 경로 구성 (static 기준)
            relative_path = os.path.relpath(full_path, STATIC_PATH).replace("\\", "/")

            # 7) image 테이블에 포스터 정보 저장
            insert_sql = """
                INSERT INTO image (uuid, img_name, path) 
                VALUES (%s, %s, %s)
            """
            cursor.execute(
                insert_sql, (unique_id, file_name, "images/movieimages/" + file_name)
            )
            conn.commit()

            # 방금 insert된 image id 가져오기 (MySQL AUTO_INCREMENT를 사용한다고 가정)
            image_id = cursor.lastrowid

            # 8) movie 테이블에 image_id 업데이트
            update_sql = "UPDATE movie SET image_id = %s WHERE mid = %s"
            cursor.execute(update_sql, (image_id, mid))

            # 9) 스틸컷(backdrops) 이미지 최대 10장까지 저장
            if movie_id:
                try:
                    detail_url = DETAIL_API_URL.format(movie_id=movie_id)
                    detail_res = requests.get(detail_url, params={"api_key": API_KEY})
                    if detail_res.status_code == 200:
                        image_data = detail_res.json()
                        backdrops = image_data.get("backdrops", [])

                        count = 0  # 저장한 스틸컷 개수
                        for b in backdrops:
                            if count >= 10:
                                break  # 최대 10장까지만 저장
                            path = b.get("file_path")
                            if path:
                                screenshot_url = IMAGE_BASE_URL + path
                                try:
                                    insert_ss_sql = """
                                        INSERT INTO image_screenshots (image_id, screenshot_url)
                                        VALUES (%s, %s)
                                    """
                                    cursor.execute(
                                        insert_ss_sql, (image_id, screenshot_url)
                                    )
                                    count += 1
                                    print(f"[{title}] → 스틸컷 저장: {screenshot_url}")
                                    time.sleep(0.1)
                                except Exception as se:
                                    print(f"[{title}] → 스틸컷 저장 실패: {se}")
                    else:
                        print(f"[{title}] → TMDb 상세 이미지 요청 실패")
                except Exception as e:
                    print(f"[{title}] → 스틸컷 처리 오류: {e}")

            # 10) 커밋
            conn.commit()
            print(f"[{title}] → DB 저장 완료")

        except Exception as e:
            print(f"[ERROR] {title} 처리 중 오류 발생: {e}")
            conn.rollback()

# 11) 마무리 정리
finally:
    driver.quit()
    cursor.close()
    conn.close()
    print("\n[모든 작업 완료]")
