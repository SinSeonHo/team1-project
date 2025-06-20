from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import os
from urllib.request import urlretrieve
import cx_Oracle
import re
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
    # 검색 페이지 접속
    search_url = f"https://search.naver.com/search.naver?query={title}+포토"
    driver.get(search_url)
    time.sleep(2)  # 페이지 로딩 대기

    # 이미지 추출
    try:
        img = driver.find_elements(
            By.CSS_SELECTOR, "._image_base_poster li.item._item img"
        )[0]
        poster_url = img.get_attribute("src")

        # 저장
        file_name = f"{title}.jpg"
        full_path = os.path.join(BASE_PATH, file_name)
        urlretrieve(poster_url, full_path)
        print(f"{full_path} 저장 완료")

    except Exception as e:
        print(f"[ERROR] {title} - {e}")

driver.quit()


cursor.close()
conn.close()
