import requests
from bs4 import BeautifulSoup
import urllib.parse
import mysql.connector
import time

# MySQL DB 연결
conn = mysql.connector.connect(
    host="localhost",
    user="ott_test",
    password="12345",
    database="ott_test",
    charset="utf8mb4",
)
cursor = conn.cursor()

# synopsis 없는 영화 가져오기 (컬럼명 수정)
cursor.execute(
    "SELECT mid, title, open_date FROM movie WHERE synopsis IS NULL OR synopsis = ''"
)
movies = cursor.fetchall()


# 날짜 형식 변환 (openDate는 문자열 "yyyy-MM-dd" 예상)
def format_date(open_date_str):
    if not open_date_str:
        return None
    return open_date_str.replace("-", "")  # 2025-06-20 -> 20250620


# 줄거리 크롤링
def get_synopsis(title):
    try:
        query = urllib.parse.quote(title + " 정보")  # '정보' 추가로 상세페이지 유도
        search_url = f"https://search.naver.com/search.naver?query={query}"
        res = requests.get(search_url, headers={"User-Agent": "Mozilla/5.0"})
        soup = BeautifulSoup(res.text, "html.parser")

        # 줄거리가 있는 태그 선택
        story_tag = soup.select_one("p.text._content_text")
        if story_tag:
            return story_tag.get_text(strip=True)
        else:
            print(f"줄거리 태그 없음: {title}")
            return None

    except Exception as e:
        print(f"에러 - {title}: {e}")
        return None


# 영화 줄거리 업데이트
for mid, title, open_date in movies:
    openDt_str = format_date(open_date)
    print(f"[{mid}] {title} ({openDt_str}) 줄거리 수집 중...")

    synopsis = get_synopsis(title)
    if synopsis:
        cursor.execute("UPDATE movie SET synopsis = %s WHERE mid = %s", (synopsis, mid))
        conn.commit()
        print(f"저장 완료: {title}")
    else:
        print(f"줄거리 없음 또는 불일치: {title}")
    time.sleep(1)

cursor.close()
conn.close()
print("모든 줄거리 크롤링 완료")
