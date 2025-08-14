import requests
import mysql.connector
import time
import re
from bs4 import BeautifulSoup
import html

# 1. MySQL DB 연결 설정
conn = mysql.connector.connect(
    host="localhost",
    user="ott_test",
    password="12345",
    database="ott_test",
    charset="utf8mb4",
)
cursor = conn.cursor()

# 2. 앱 ID와 게임 제목 가져오기 (synopsis 비어있는 경우)
cursor.execute(
    "SELECT gid, appid, title FROM game WHERE synopsis IS NULL OR synopsis = ''"
)
games = cursor.fetchall()


# 3. HTML 및 특수문자 정리 함수
def clean_text(text):
    text = BeautifulSoup(text, "html.parser").get_text(separator="\n")  # HTML 태그 제거
    text = html.unescape(text)  # HTML 엔티티 제거
    text = re.sub(r"[ \u200b\xa0\t\r\f\v]+", " ", text)  # 특수공백 제거
    text = re.sub(r"\s{2,}", " ", text)  # 여러 공백 하나로
    return text.strip()


# 4. Steam API에서 시놉시스 요청
def get_synopsis_from_steam(appid):
    try:
        url = f"https://store.steampowered.com/api/appdetails?appids={appid}&l=koreana"
        res = requests.get(url)
        data = res.json()

        if str(appid) in data and data[str(appid)]["success"]:
            desc_html = data[str(appid)]["data"].get("detailed_description", "")
            if desc_html:
                return clean_text(desc_html)
        return None
    except Exception as e:
        print(f"에러 (appid={appid}): {e}")
        return None


# 5. DB 저장 루프
for gid, appid, title in games:
    print(f"[{gid}] {title} (appid: {appid}) 시놉시스 요청 중...")

    synopsis = get_synopsis_from_steam(appid)
    if synopsis:
        cursor.execute("UPDATE game SET synopsis = %s WHERE gid = %s", (synopsis, gid))
        conn.commit()
        print(f"저장 완료: {title}")
    else:
        print(f"시놉시스 없음 또는 불일치: {title}")
    # time.sleep(1)

# 6. 마무리
cursor.close()
conn.close()
print("모든 게임 시놉시스 저장 완료")
