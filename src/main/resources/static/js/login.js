       // 페이지가 완전히 로드 된 이후에 이벤트 생성
        document.addEventListener("DOMContentLoaded", function () {
          const loginTab = document.getElementById("loginTab");
          const registerTab = document.getElementById("registerTab");
          const loginForm = document.getElementById("loginForm");
          const registerForm = document.getElementById("registerForm");
          // 로그인 탭을 누를 시
          loginTab.addEventListener("click", function () {
            loginTab.classList.add("active");
            registerTab.classList.remove("active");
            loginForm.classList.remove("d-none");
            registerForm.classList.add("d-none");
          });
          // 회원가입 탭을 누를 시
          registerTab.addEventListener("click", function () {
            registerTab.classList.add("active");
            loginTab.classList.remove("active");
            registerForm.classList.remove("d-none");
            loginForm.classList.add("d-none");
          });
        });