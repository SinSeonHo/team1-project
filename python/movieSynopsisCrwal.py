import os
import requests
from bs4 import BeautifulSoup
import urllib.parse
import mysql.connector
import time

# 1. DB 연결 (환경변수 사용)
conn = mysql.connector.connect(
    host=os.getenv("DB_HOST", "127.0.0.1"),
    port=int(os.getenv("DB_PORT", 3306)),
    user=os.getenv("DB_USER", "ott_test"),
    password=os.getenv("DB_PASS", "12345"),
    database=os.getenv("DB_NAME", "ott_test"),
    charset="utf8mb4",
    use_unicode=True,
)
cursor = conn.cursor()

# 2. synopsis 없는 영화 가져오기
cursor.execute(
    "SELECT mid, title, open_date FROM movie WHERE synopsis IS NULL OR synopsis = ''"
)
movies = cursor.fetchall()

# 3. 날짜 포맷 변환
def format_date(open_date_str):
    if not open_date_str:
        return None
    return open_date_str.replace("-", "")

# 4. 줄거리 크롤링
def get_synopsis(title):
    try:
        query = urllib.parse.quote(title + " 정보")
        search_url = f"https://search.naver.com/search.naver?query={query}"
        res = requests.get(search_url, headers={"User-Agent": "Mozilla/5.0"}, timeout=5)
        soup = BeautifulSoup(res.text, "html.parser")

        story_tag = soup.select_one("p.text._content_text")
        if story_tag:
            return story_tag.get_text(strip=True)
        else:
            print(f"줄거리 태그 없음: {title}")
            return None

    except Exception as e:
        print(f"에러 - {title}: {e}")
        return None

# 5. 각 영화 처리
for mid, title, open_date in movies:
    openDt_str = format_date(open_date)
    print(f"[{mid}] {title} ({openDt_str}) 줄거리 수집 중...")

    synopsis = get_synopsis(title)
    if synopsis:
        try:
            cursor.execute(
                "UPDATE movie SET synopsis = %s WHERE mid = %s",
                (synopsis, mid),
            )
            conn.commit()
            print(f"저장 완료: {title}")
        except Exception as e:
            print(f"[ERROR] {title} DB 저장 실패: {e}")
            conn.rollback()
    else:
        print(f"줄거리 없음 또는 불일치: {title}")

    time.sleep(1)

# 6. 종료
cursor.close()
conn.close()
print("모든 줄거리 크롤링 완료")
