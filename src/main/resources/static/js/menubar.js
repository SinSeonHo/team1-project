document.addEventListener("DOMContentLoaded", () => {
  const menuItems = document.querySelectorAll(".header__menu ul li");

  menuItems.forEach((item) => {
    const link = item.querySelector("a");
    if (!link) return;

    const currentPath = window.location.pathname;
    const linkPath = link.getAttribute("href");

    if (linkPath === "/") {
      // 루트 경로일 때만 정확하게 현재 경로가 '/' 인 경우 active 처리
      if (currentPath === "/") {
        item.classList.add("active");
      } else {
        item.classList.remove("active");
      }
    } else {
      // 루트가 아닌 경우에만 startsWith 체크
      if (currentPath.startsWith(linkPath)) {
        item.classList.add("active");
      } else {
        item.classList.remove("active");
      }
    }
  });
});
