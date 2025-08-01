import os
import requests
import cx_Oracle
import time
import uuid
import re
from selenium import webdriver
from selenium.webdriver.common.by import By
import shutil

# 1. 이미지 저장 경로 설정
BASE_PATH = r"C:/upload/images/movieimages"
STATIC_PATH = os.path.abspath("../src/main/resources/static")
os.makedirs(BASE_PATH, exist_ok=True)

# 2. Oracle DB 연결
dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
cursor = conn.cursor()

# 3. TMDb API 설정
API_KEY = "cfa721d14c0aa4e4b28b3f26e81369f9"
SEARCH_API_URL = "https://api.themoviedb.org/3/search/movie"
DETAIL_API_URL = "https://api.themoviedb.org/3/movie/{movie_id}/images"
IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

# 4. Selenium (크롬 헤드리스)
options = webdriver.ChromeOptions()
options.add_argument("--headless")
driver = webdriver.Chrome(options=options)

# 5. 이미지가 없는 영화 조회
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
        movie_id = None  # TMDb의 고유 movie_id 저장용
        backdrops = []

        # 1) TMDb 검색
        try:
            print(f"[{title}] → TMDb 검색 시도 중...")
            params = {
                "query": title,
                "language": "ko-KR",
                "api_key": API_KEY,
            }
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

        # 2) TMDb 실패 → 네이버 이미지 검색
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

        # 3) 포스터 없으면 건너뜀
        if not poster_url:
            print(f"[{title}] → 최종적으로 포스터 없음 → 건너뜀")
            continue

        try:
            # 안전한 파일명 생성
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

            # 이미지 다운로드
            if download_image(poster_url, full_path):
                print(f"[{title}] → 이미지 저장 완료: {file_name}")
            else:
                print(f"[{title}] → 이미지 저장 실패: {file_name}")
                continue

            # static 기준 상대경로
            relative_path = os.path.relpath(full_path, STATIC_PATH).replace("\\", "/")

            # 이미지 테이블 저장
            output_inum = cursor.var(cx_Oracle.NUMBER)
            cursor.execute(
                """
                INSERT INTO image (uuid, img_name, path) 
                VALUES (:uuid, :img_name, :path)
                RETURNING inum INTO :output_inum
                """,
                {
                    "uuid": unique_id,
                    "img_name": file_name,
                    "path": "images/movieimages/" + file_name,
                    "output_inum": output_inum,
                },
            )

            image_id = int(output_inum.getvalue()[0])

            # movie 테이블 업데이트
            cursor.execute(
                "UPDATE movie SET image_id = :imgid WHERE mid = :mid",
                {"imgid": image_id, "mid": mid},
            )

            # backdrops(스틸컷) 정보 요청 및 저장
            if movie_id:
                try:
                    detail_url = DETAIL_API_URL.format(movie_id=movie_id)
                    detail_res = requests.get(detail_url, params={"api_key": API_KEY})
                    if detail_res.status_code == 200:
                        image_data = detail_res.json()
                        backdrops = image_data.get("backdrops", [])
                        for b in backdrops:
                            path = b.get("file_path")
                            if path:
                                screenshot_url = IMAGE_BASE_URL + path
                                try:
                                    cursor.execute(
                                        """
                                        INSERT INTO image_screenshots (image_id, screenshot_url)
                                        VALUES (:image_id, :screenshot_url)
                                        """,
                                        {
                                            "image_id": image_id,
                                            "screenshot_url": screenshot_url,
                                        },
                                    )
                                    print(f"[{title}] → 스틸컷 저장: {screenshot_url}")
                                    time.sleep(0.1)
                                except Exception as se:
                                    print(f"[{title}] → 스틸컷 저장 실패: {se}")
                    else:
                        print(f"[{title}] → TMDb 상세 이미지 요청 실패")
                except Exception as e:
                    print(f"[{title}] → 스틸컷 처리 오류: {e}")

            conn.commit()
            print(f"[{title}] → DB 저장 완료")

        except Exception as e:
            print(f"[ERROR] {title} 처리 중 오류 발생: {e}")
            conn.rollback()

finally:
    driver.quit()
    cursor.close()
    conn.close()
    print("\n[모든 작업 완료]")
