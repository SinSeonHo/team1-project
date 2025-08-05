import os
import requests
from urllib.request import urlretrieve
import cx_Oracle
import uuid
import re
import time

# 1. 이미지 저장 경로 설정
BASE_PATH = r"C:/upload/images/gameimages"  # 실제 이미지 저장 경로
STATIC_PATH = os.path.abspath(
    "../src/main/resources/static"
)  # static 기준 상대경로 계산용
os.makedirs(BASE_PATH, exist_ok=True)  # 디렉토리 없으면 생성

# 2. Oracle DB 연결
dsn = cx_Oracle.makedsn("localhost", 1521, service_name="XE")
conn = cx_Oracle.connect(user="ott_test", password="12345", dsn=dsn, encoding="UTF-8")
cursor = conn.cursor()

# 3. 이미지가 없는 게임 정보 가져오기
cursor.execute("SELECT gid, title, appid FROM game WHERE image_id IS NULL")
games = cursor.fetchall()


# 4. 이미지 다운로드 함수 정의
def download_image(url, full_path, title):
    """
    주어진 URL에서 이미지를 다운로드하고 지정된 경로에 저장합니다.
    """
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


# 5. 각 게임 이미지 처리 루프
for gid, title, appid in games:
    print(f"\n[{title}] 이미지 수집 시작...")

    # 파일 이름 안전화
    unique_id = str(uuid.uuid4())
    safe_title = re.sub(r"[^a-zA-Z0-9_-]", "", title.strip()).strip("_- ")
    ext = "jpg"
    file_name = (
        f"{unique_id}_{safe_title}.{ext}" if safe_title else f"{unique_id}.{ext}"
    )
    full_path = os.path.join(BASE_PATH, file_name)

    # 1순위: capsule 이미지
    capsule_url = (
        f"https://cdn.cloudflare.steamstatic.com/steam/apps/{appid}/capsule_616x353.jpg"
    )
    success = download_image(capsule_url, full_path, title)

    # 실패 시: header_image로 대체
    data = None
    if not success:
        print(f"[{title}] capsule 실패 → header_image 시도")
        try:
            time.sleep(0.5)
            res = requests.get(
                f"https://store.steampowered.com/api/appdetails?appids={appid}",
                timeout=5,
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
        # capsule 성공 시도 → API 요청은 그래도 진행하여 screenshots 추출
        try:
            time.sleep(0.5)
            res = requests.get(
                f"https://store.steampowered.com/api/appdetails?appids={appid}",
                timeout=5,
            )
            data = res.json()
        except Exception as e:
            print(f"[{title}] Storefront API 호출 오류(스크린샷용): {e}")
            data = None

    if not success:
        print(f"[{title}] 이미지 수집 실패 - 건너뜀")
        continue

    # 6. 이미지 정보 DB 저장
    try:
        # static 기준 상대경로 구하기
        relative_path = os.path.relpath(full_path, STATIC_PATH).replace("\\", "/")

        # image 테이블 삽입 + inum 반환
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
                "path": "images/gameimages/" + file_name,
                "output_inum": output_inum,
            },
        )

        image_id = int(output_inum.getvalue()[0])

        # game 테이블 업데이트
        cursor.execute(
            "UPDATE game SET image_id = :imgid WHERE gid = :gid",
            {"imgid": image_id, "gid": gid},
        )

        # 7. screenshots 테이블 처리 (최대 10개)
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
                                cursor.execute(
                                    """
                                    INSERT INTO image_screenshots (image_id, screenshot_url)
                                    VALUES (:image_id, :screenshot_url)
                                    """,
                                    {
                                        "image_id": image_id,
                                        "screenshot_url": url,
                                    },
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
