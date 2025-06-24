import os
import requests
from urllib.request import urlretrieve
import cx_Oracle
import time
import uuid
import re

# 이미지 저장 경로
BASE_PATH = os.path.abspath("./src/main/resources/static/images/gameimages")
os.makedirs(BASE_PATH, exist_ok=True)

# 오라클 DB 연결
dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
cursor = conn.cursor()

# 이미지 없는 게임 조회
cursor.execute("SELECT gid, title, appid FROM game WHERE image_id IS NULL")
games = cursor.fetchall()

for gid, title, appid in games:
    print(f"[{title}] 이미지 처리 시작...")

    # 이미지 저장 준비
    unique_id = str(uuid.uuid4())
    safe_title = re.sub(r"[^a-zA-Z0-9_-]", "", title.strip())
    safe_title = safe_title.strip("_- ")
    ext = "jpg"
    file_name = (
        f"{unique_id}_{safe_title}.{ext}" if safe_title else f"{unique_id}.{ext}"
    )
    full_path = os.path.join(BASE_PATH, file_name)

    # 1순위: capsule_616x353.jpg 다운로드 시도
    capsule_url = (
        f"https://cdn.cloudflare.steamstatic.com/steam/apps/{appid}/capsule_616x353.jpg"
    )
    image_url = capsule_url

    def download_image(url):
        try:
            # 먼저 HEAD 요청으로 이미지 존재 확인
            response = requests.head(url)
            if response.status_code == 200:
                urlretrieve(url, full_path)
                print(f"[{title}] 이미지 저장 완료: {file_name}")
                return True
            return False
        except Exception as e:
            print(f"[{title}] 이미지 다운로드 실패: {e}")
            return False

    # 시도 1: capsule 이미지
    success = download_image(image_url)

    # 시도 2: Steam Storefront API (header_image)
    if not success:
        print(f"[{title}] capsule 이미지 실패, header_image로 대체 시도")
        try:
            res = requests.get(
                f"https://store.steampowered.com/api/appdetails?appids={appid}"
            )
            data = res.json()
            header_img = (
                data.get(str(appid), {}).get("data", {}).get("header_image", None)
            )

            if header_img:
                success = download_image(header_img)
            else:
                print(f"[{title}] header_image 정보 없음")

        except Exception as e:
            print(f"[{title}] Storefront API 호출 실패: {e}")
            success = False

    if not success:
        print(f"[{title}] 이미지 수집 실패 - 건너뜀")
        continue

    # DB 저장
    try:
        cursor.execute(
            "INSERT INTO image (img_name, path, gid, uuid) VALUES (:img_name, :path, :gid, :uuid)",
            {
                "img_name": file_name,
                "path": full_path,
                "gid": gid,
                "uuid": unique_id,
            },
        )

        cursor.execute("SELECT MAX(inum) FROM image")
        image_id = cursor.fetchone()[0]

        cursor.execute(
            "UPDATE game SET image_id = :imgid WHERE gid = :gid",
            {"imgid": image_id, "gid": gid},
        )

        conn.commit()
        print(f"[{title}] DB 저장 및 연결 완료")

    except Exception as e:
        print(f"[ERROR] {title} DB 저장 실패: {e}")

# 마무리
cursor.close()
conn.close()
print("모든 작업 완료")
