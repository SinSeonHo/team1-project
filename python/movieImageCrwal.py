from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import os
from urllib.request import urlretrieve
import cx_Oracle
import time

# 다운로드 경로 설정
BASE_PATH = os.path.abspath("./src/main/resources/images/movieimages")
if not os.path.exists(BASE_PATH):
    os.makedirs(BASE_PATH)

# 오라클 DB 연결
dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
cursor = conn.cursor()

# 이미지 없는 영화 조회
cursor.execute("SELECT mid, title FROM movie WHERE image_id IS NULL")
movies = cursor.fetchall()

options = webdriver.ChromeOptions()
options.add_argument("--headless")
driver = webdriver.Chrome(options=options)

wait = WebDriverWait(driver, 10)  # 최대 10초 대기

for mid, title in movies:
    print(f"[{title}] 이미지 검색 중...")

    search_url = f"https://search.naver.com/search.naver?query={title}+포토"
    driver.get(search_url)
    time.sleep(2)  # 페이지 로딩 대기

    try:
        # 이미지 리스트 중 첫번째 유효 이미지 src 가져오기
        imgs = driver.find_elements(
            By.CSS_SELECTOR, ".area_card._image_base_poster ul li a img"
        )

        poster_url = None
        for img in imgs:
            poster_url = img.get_attribute("src") or img.get_attribute("data-src")
            if poster_url and poster_url.strip() != "":
                break

        if not poster_url:
            print(f"[{title}] 유효한 이미지 없음 - 건너뜀")
            continue

        # 파일명 안전하게 처리 (특수문자 제거)
        safe_title = "".join(c for c in title if c.isalnum() or c in " _-")
        ext = poster_url.split(".")[-1].split("?")[0]
        file_name = f"{safe_title}.{ext}"
        full_path = os.path.join(BASE_PATH, file_name)

        # 이미지 다운로드
        urlretrieve(poster_url, full_path)
        print(f"[{title}] 이미지 저장 완료: {full_path}")

        # DB 삽입 (컬럼명 img_name, path 기준)
        cursor.execute(
            """
            INSERT INTO image (img_name, path)
            VALUES (:img_name, :path)
            """,
            {"img_name": file_name, "path": full_path},
        )

        # image_id 가져오기
        cursor.execute("SELECT MAX(inum) FROM image")
        image_id = cursor.fetchone()[0]

        # movie 테이블에 image_id 업데이트
        cursor.execute(
            "UPDATE movie SET image_id = :imgid WHERE mid = :mid",
            {"imgid": image_id, "mid": mid},
        )

        conn.commit()
        print(f"[{title}] 이미지 저장 및 연결 완료")

    except Exception as e:
        print(f"[ERROR] {title} 처리 실패: {e}")

driver.quit()
cursor.close()
conn.close()
print("모든 작업 완료")
