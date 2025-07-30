const contentOptions = {
  m: [
    { code: "t", name: "제목" },
    { code: "d", name: "감독" },
    { code: "a", name: "배우들" },
  ],
  g: [
    { code: "t", name: "제목" },
    { code: "d", name: "개발사" },
    { code: "p", name: "배급사" },
  ],
};
const action = {
  m: "/api/movies/list",
  g: "/api/games/list",
};

function updateCityOptions() {
  const content = document.getElementById("content").value;
  const typeSelect = document.getElementById("type");
  typeSelect.textContent = "제목";
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
}
