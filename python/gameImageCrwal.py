import os
import requests
from urllib.request import urlretrieve
import cx_Oracle
import uuid
import re

# 이미지 저장 경로
BASE_PATH = os.path.abspath("../src/main/resources/static/images/gameimages")
STATIC_PATH = os.path.abspath("../src/main/resources/static")  # static 기준 경로
os.makedirs(BASE_PATH, exist_ok=True)

# Oracle DB 연결
dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
cursor = conn.cursor()

# 이미지 없는 게임 가져오기
cursor.execute("SELECT gid, title, appid FROM game WHERE image_id IS NULL")
games = cursor.fetchall()

for gid, title, appid in games:
    print(f"[{title}] 이미지 수집 시작...")

    unique_id = str(uuid.uuid4())
    safe_title = re.sub(r"[^a-zA-Z0-9_-]", "", title.strip())
    safe_title = safe_title.strip("_- ")
    ext = "jpg"
    file_name = (
        f"{unique_id}_{safe_title}.{ext}" if safe_title else f"{unique_id}.{ext}"
    )
    full_path = os.path.join(BASE_PATH, file_name)

    # 1순위: Steam capsule 이미지
    capsule_url = (
        f"https://cdn.cloudflare.steamstatic.com/steam/apps/{appid}/capsule_616x353.jpg"
    )

    def download_image(url):
        try:
            head = requests.head(url, timeout=5)
            if head.status_code == 200:
                urlretrieve(url, full_path)
                print(f"[{title}] 이미지 저장 완료: {file_name}")
                return True
            return False
        except Exception as e:
            print(f"[{title}] 이미지 다운로드 실패: {e}")
            return False

    success = download_image(capsule_url)

    # 실패 시: Steam Storefront API header_image 사용
    if not success:
        print(f"[{title}] capsule 실패 → header_image 시도")
        try:
            res = requests.get(
                f"https://store.steampowered.com/api/appdetails?appids={appid}",
                timeout=5,
            )
            data = res.json()
            header_img = data.get(str(appid), {}).get("data", {}).get("header_image")
            if header_img:
                success = download_image(header_img)
            else:
                print(f"[{title}] header_image 없음")
        except Exception as e:
            print(f"[{title}] Storefront API 호출 오류: {e}")
            success = False

    if not success:
        print(f"[{title}] 이미지 수집 실패 - 건너뜀")
        continue

    # DB 저장
    try:
        # static 경로 기준 상대경로 구하기
        relative_path = os.path.relpath(full_path, STATIC_PATH).replace("\\", "/")

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
                "path": relative_path,  # static부터 시작하는 상대경로 저장
                "output_inum": output_inum,
            },
        )

        image_id = int(output_inum.getvalue()[0])

        # game 테이블 업데이트
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
print("모든 게임 이미지 처리 완료")
