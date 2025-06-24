const btn = document.querySelector(".follow-btn");

btn.addEventListener("click", () => {
  const isFollow = btn.classList.toggle("follow");
  btn.textContent = isFollow ? "Follow" : "Followed";
});
