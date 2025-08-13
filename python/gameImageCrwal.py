import os
import requests
from urllib.request import urlretrieve
# import cx_Oracle
import mysql.connector
import uuid
import re
import time

# 1. 이미지 저장 경로 설정
BASE_PATH = r"C:/upload/images/gameimages"
STATIC_PATH = os.path.abspath("../src/main/resources/static")
os.makedirs(BASE_PATH, exist_ok=True)

# 2. DB 연결
# ===== [Oracle 전용] =====
# dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
# conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
# cursor = conn.cursor()

# ===== [MySQL 전용] =====
conn = mysql.connector.connect(
    host="localhost",
    port=3306,
    user="ott_test",
    password="12345",
    database="ott_test_db",
    charset="utf8mb4",
    use_unicode=True
)
cursor = conn.cursor()

# 3. 이미지가 없는 게임 정보 가져오기
cursor.execute("SELECT gid, title, appid FROM game WHERE image_id IS NULL")
games = cursor.fetchall()

# 4. 이미지 다운로드 함수
def download_image(url, full_path, title):
    try:
        head = requests.head(url, allow_redirects=True, timeout=5)
        print(f"[{title}] HEAD {url} status_code: {head.status_code}")
        if head.status_code == 200:
            urlretrieve(url, full_path)
            print(f"[{title}] 이미지 저장 완료: {os.path.basename(full_path)}")
            return True
        else:
            print(f"[{title}] HEAD 요청 실패 상태코드: {head.status_code}")
            return False
    except Exception as e:
        print(f"[{title}] 이미지 다운로드 실패: {e}")
        return False

# 5. 각 게임 처리
for gid, title, appid in games:
    print(f"\n[{title}] 이미지 수집 시작...")

    unique_id = str(uuid.uuid4())
    safe_title = re.sub(r"[^a-zA-Z0-9_-]", "", title.strip()).strip("_- ")
    ext = "jpg"
    file_name = f"{unique_id}_{safe_title}.{ext}" if safe_title else f"{unique_id}.{ext}"
    full_path = os.path.join(BASE_PATH, file_name)

    # 1순위: capsule
    capsule_url = f"https://cdn.cloudflare.steamstatic.com/steam/apps/{appid}/capsule_616x353.jpg"
    success = download_image(capsule_url, full_path, title)

    # 실패 시 header_image
    data = None
    if not success:
        print(f"[{title}] capsule 실패 → header_image 시도")
        try:
            time.sleep(0.5)
            res = requests.get(
                f"https://store.steampowered.com/api/appdetails?appids={appid}",
                timeout=5
            )
            data = res.json()
            header_img = data.get(str(appid), {}).get("data", {}).get("header_image")
            if header_img:
                success = download_image(header_img, full_path, title)
            else:
                print(f"[{title}] header_image 없음")
        except Exception as e:
            print(f"[{title}] Storefront API 호출 오류: {e}")
            success = False
    else:
        try:
            time.sleep(0.5)
            res = requests.get(
                f"https://store.steampowered.com/api/appdetails?appids={appid}",
                timeout=5
            )
            data = res.json()
        except Exception as e:
            print(f"[{title}] Storefront API 호출 오류(스크린샷용): {e}")
            data = None

    if not success:
        print(f"[{title}] 이미지 수집 실패 - 건너뜀")
        continue

    try:
        relative_path = os.path.relpath(full_path, STATIC_PATH).replace("\\", "/")

        # ===== [Oracle 전용] =====
        # output_inum = cursor.var(cx_Oracle.NUMBER)
        # cursor.execute(
        #     """
        #     INSERT INTO image (uuid, img_name, path) 
        #     VALUES (:uuid, :img_name, :path)
        #     RETURNING inum INTO :output_inum
        #     """,
        #     {
        #         "uuid": unique_id,
        #         "img_name": file_name,
        #         "path": "images/gameimages/" + file_name,
        #         "output_inum": output_inum,
        #     },
        # )
        # image_id = int(output_inum.getvalue()[0])
        # cursor.execute(
        #     "UPDATE game SET image_id = :imgid WHERE gid = :gid",
        #     {"imgid": image_id, "gid": gid},
        # )

        # ===== [MySQL 전용] =====
        cursor.execute(
            """
            INSERT INTO image (uuid, img_name, path)
            VALUES (%s, %s, %s)
            """,
            (unique_id, file_name, "images/gameimages/" + file_name),
        )
        image_id = cursor.lastrowid
        cursor.execute(
            "UPDATE game SET image_id = %s WHERE gid = %s",
            (image_id, gid),
        )

        # ===== screenshots 처리 =====
        try:
            if data and "data" in data.get(str(appid), {}):
                game_data = data[str(appid)]["data"]
                screenshots = game_data.get("screenshots", [])

                if screenshots:
                    count = 0
                    for shot in screenshots:
                        if count >= 10:
                            break
                        url = shot.get("path_thumbnail")
                        if url:
                            try:
                                # ===== [Oracle 전용] =====
                                # cursor.execute(
                                #     """
                                #     INSERT INTO image_screenshots (image_id, screenshot_url)
                                #     VALUES (:image_id, :screenshot_url)
                                #     """,
                                #     {
                                #         "image_id": image_id,
                                #         "screenshot_url": url,
                                #     },
                                # )

                                # ===== [MySQL 전용] =====
                                cursor.execute(
                                    """
                                    INSERT INTO image_screenshots (image_id, screenshot_url)
                                    VALUES (%s, %s)
                                    """,
                                    (image_id, url),
                                )
                                print(f"[{title}] 스크린샷 저장: {url}")
                                time.sleep(0.1)
                                count += 1
                            except Exception as se:
                                print(f"[{title}] 스크린샷 저장 실패: {url} → {se}")
                else:
                    print(f"[{title}] screenshots 데이터 없음")
            else:
                print(f"[{title}] Storefront API의 data 필드 없음")
        except Exception as e:
            print(f"[{title}] 스크린샷 처리 오류: {e}")

        conn.commit()
        print(f"[{title}] DB 저장 완료")

    except Exception as e:
        print(f"[ERROR] {title} DB 저장 실패: {e}")
        conn.rollback()

# 8. 마무리
cursor.close()
conn.close()
print("\n[모든 게임 이미지 처리 완료]")
