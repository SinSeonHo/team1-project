import os
import requests
from urllib.request import urlretrieve
import cx_Oracle
import time
import uuid
import re
from selenium import webdriver
from selenium.webdriver.common.by import By

# 이미지 저장 경로
BASE_PATH = os.path.abspath("./src/main/resources/static/images/movieimages")
STATIC_PATH = os.path.abspath("./src/main/resources/static")  # static 기준 경로
os.makedirs(BASE_PATH, exist_ok=True)

# Oracle DB 연결
dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
cursor = conn.cursor()

# TMDb API 설정
API_KEY = "cfa721d14c0aa4e4b28b3f26e81369f9"
BASE_API_URL = "https://api.themoviedb.org/3/search/movie"
IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

# Selenium (크롬 헤드리스 설정)
options = webdriver.ChromeOptions()
options.add_argument("--headless")
driver = webdriver.Chrome(options=options)

# 이미지가 없는 영화 조회
cursor.execute("SELECT mid, title FROM movie WHERE image_id IS NULL")
movies = cursor.fetchall()

for mid, title in movies:
    print(f"[{title}] 이미지 검색 시작...")

    poster_url = None

    # 1) 네이버 이미지 검색 시도
    try:
        search_url = f"https://search.naver.com/search.naver?query={title}+포스터"
        driver.get(search_url)
        time.sleep(2)

        imgs = driver.find_elements(
            By.CSS_SELECTOR, ".area_card._image_base_poster ul li a img"
        )
        for img in imgs:
            poster_url = img.get_attribute("src") or img.get_attribute("data-src")
            if poster_url and poster_url.strip():
                break

        if not poster_url:
            print(f"[{title}] 네이버에서 유효한 이미지 없음 → TMDb로 전환")

    except Exception as e:
        print(f"[{title}] 네이버 이미지 검색 오류: {e}")

    # 2) TMDb 검색
    if not poster_url:
        try:
            params = {
                "query": title,
                "language": "ko-KR",
                "api_key": API_KEY,
            }
            response = requests.get(BASE_API_URL, params=params)
            data = response.json()

            if data["results"]:
                matched = None
                for result in data["results"]:
                    if result.get("title", "").strip().lower() == title.strip().lower():
                        matched = result
                        break
                if not matched:
                    matched = data["results"][0]

                poster_path = matched.get("poster_path")
                if poster_path:
                    poster_url = IMAGE_BASE_URL + poster_path
                    print(
                        f"[{title}] TMDb에서 포스터 찾음 (매칭된 제목: {matched.get('title')})"
                    )
                else:
                    print(f"[{title}] TMDb에 포스터 없음")
            else:
                print(f"[{title}] TMDb 검색 결과 없음")

        except Exception as e:
            print(f"[{title}] TMDb 오류: {e}")
            poster_url = None

    # 포스터 URL 없으면 패스
    if not poster_url:
        print(f"[{title}] 포스터 없음 → 건너뜀")
        continue

    try:
        # 안전한 파일명 생성 (한글 제거)
        unique_id = str(uuid.uuid4())
        safe_title = re.sub(r"[^a-zA-Z0-9_-]", "", title.strip())
        ext = poster_url.split(".")[-1].split("?")[0]
        file_name = (
            f"{unique_id}_{safe_title}.{ext}" if safe_title else f"{unique_id}.{ext}"
        )
        full_path = os.path.join(BASE_PATH, file_name)

        # 이미지 저장
        urlretrieve(poster_url, full_path)
        print(f"[{title}] 이미지 저장 완료: {file_name}")

        # static부터 시작하는 상대경로 구하기
        relative_path = os.path.relpath(full_path, STATIC_PATH).replace("\\", "/")

        # DB 저장 (RETURNING inum INTO 사용)
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
                "path": relative_path,
                "output_inum": output_inum,
            },
        )

        image_id = int(output_inum.getvalue()[0])

        # movie 테이블과 연결
        cursor.execute(
            "UPDATE movie SET image_id = :imgid WHERE mid = :mid",
            {"imgid": image_id, "mid": mid},
        )

        conn.commit()
        print(f"[{title}] DB 저장 및 연결 완료")

    except Exception as e:
        print(f"[ERROR] {title} 처리 중 오류 발생: {e}")

# 마무리
driver.quit()
cursor.close()
conn.close()
print("✅ 모든 작업 완료")
