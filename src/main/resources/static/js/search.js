// const contentOptions = {
//   m: [
//     { code: "t", name: "제목" },
//     { code: "g", name: "장르" },
//     { code: "d", name: "감독명" },
//     { code: "a", name: "배우명" },
//   ],
//   g: [
//     { code: "t", name: "제목" },
//     { code: "g", name: "장르" },
//     { code: "d", name: "개발사" },
//     { code: "p", name: "배급사" },
//   ],
// };
// const action = {
//   m: "/api/movies/list",
//   g: "/api/games/list",
// };

// function updateCityOptions() {
//   const typeSelect = document.getElementById("type");
//   const content = document.getElementById("content").value;
//   typeSelect.innerHTML = "";
//   // 컨텐츠에 해당하는 키워드 목록 추가
//   if (content && contentOptions[content]) {
//     contentOptions[content].forEach((city) => {
//       const option = document.createElement("option");
//       option.value = city.code;
//       option.textContent = city.name;
//       typeSelect.appendChild(option);
//     });
//     document.querySelector(".search-box").action = action[content];
//   }
// }

window.addEventListener("DOMContentLoaded", () => {
  // const content = document.getElementById("content"); // 선택 콘텐츠 타입
  // const path = window.location.pathname.toLowerCase(); // 경로 가져오기
  const searchQuery = window.location.search; // ?content=g&type=t&keyword=fall

  // if (path.includes("movies")) {
  //   content.value = "m";
  // } else if (path.includes("games")) {
  //   content.value = "g";
  // }
  // updateCityOptions();

  // type 가져오기
  // if (searchQuery.includes("type=")) {
  //   const type = searchQuery.split("type=");
  //   document.getElementById("type").value = type[1].split("&")[0];
  // }

  // keyword 가져오기
  if (searchQuery.includes("keyword=")) {
    const type = searchQuery.split("keyword=");
    let encodedValue = type[1].split("&")[0]; // %91%82 형태로 나옴
    encodedValue = encodedValue.replaceAll("+", " ");
    document.getElementById("search").value = decodeURIComponent(encodedValue); // 검색창에 인코딩 해서 한글 띄움
  }
});
