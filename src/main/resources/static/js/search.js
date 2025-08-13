const contentOptions = {
  m: [
    { code: "t", name: "제목" },
    { code: "g", name: "장르" },
    { code: "d", name: "감독명" },
    { code: "a", name: "배우명" },
  ],
  g: [
    { code: "t", name: "제목" },
    { code: "g", name: "장르" },
    { code: "d", name: "개발사" },
    { code: "p", name: "배급사" },
  ],
};
const action = {
  m: "/api/movies/list",
  g: "/api/games/list",
};

function updateCityOptions() {
  try { // 회원정보 입력 페이지에서 에러날 때 임시 방편
    const content = document.getElementById("content").value;
    const typeSelect = document.getElementById("type");
    typeSelect.innerHTML = "";
    // 컨텐츠에 해당하는 키워드 목록 추가
    if (content && contentOptions[content]) {
      contentOptions[content].forEach((city) => {
        const option = document.createElement("option");
        option.value = city.code;
        option.textContent = city.name;
        typeSelect.appendChild(option);
      });
      document.querySelector(".search-box").action = action[content];
    }
  } catch (error) {
    return ;
  }
}

window.addEventListener("DOMContentLoaded", () => {
  const content = document.getElementById("content"); // 선택 컨텐츠 타입
  const path = window.location.pathname.toLowerCase(); // 경로 가져오기
  const searchQuery = window.location.search;

  if (path.includes("movies")) {
    content.value = "m";
  } else if (path.includes("games")) {
    content.value = "g";
  }
  updateCityOptions();

  if (searchQuery.includes("type=")) {
    const type = searchQuery.split("type=");
    document.getElementById("type").value = type[1].split("&")[0];
  }
});
