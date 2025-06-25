       // 페이지가 완전히 로드 된 이후에 이벤트 생성
        document.addEventListener("DOMContentLoaded", function () {
          const loginTab = document.getElementById("loginTab");
          const registerTab = document.getElementById("registerTab");
          const loginForm = document.getElementById("loginForm");
          const registerForm = document.getElementById("registerForm");
          const title = document.getElementById("title");

          // 로그인 탭을 누를 시
          loginTab.addEventListener("click", function () {
            loginTab.classList.add("active");
            registerTab.classList.remove("active");
            loginForm.classList.remove("really-none");
            registerForm.classList.add("really-none");
            // title.innerText("로그인")
            title.textContent = "로그인"
          });
          // 회원가입 탭을 누를 시
          registerTab.addEventListener("click", function () {
            registerTab.classList.add("active");
            loginTab.classList.remove("active");
            registerForm.classList.remove("really-none");
            loginForm.classList.add("really-none");
            title.textContent = "회원가입"
          });

          const googleBtn = document.querySelector("#google-login");
          const kakaoBtn = document.querySelector("#kakao-login");
          const naverBtn = document.querySelector("#naver-login");

          googleBtn.addEventListener("click", () => {
          url = "/oauth2/authorization/google"
          window.location.href = url;
          })

          kakaoBtn.addEventListener("click", () => {
          url = "/oauth2/authorization/kakao"
          window.location.href = url;
          })

          naverBtn.addEventListener("click", () => {
          url = "/oauth2/authorization/naver"
          window.location.href = url;
          })
        });

        