const cityOptions = {
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

function updateCityOptions() {
  const country = document.getElementById("content").value;
  const typeSelect = document.getElementById("type");
  typeSelect.textContent = "123";
  // 나라에 해당하는 도시 목록 추가
  if (country && cityOptions[country]) {
    cityOptions[country].forEach((city) => {
      const option = document.createElement("option");
      option.value = city.code;
      option.textContent = city.name;
      typeSelect.appendChild(option);
    });
  }
}
