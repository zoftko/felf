const getBtnSpinner = () => {
  return document.querySelector("#token-form button > span.loading");
};
const getBtnText = () => {
  return document.querySelector("#token-form button > span.font-normal");
};

document.body.addEventListener("htmx:responseError", (event) => {
  const request = event.detail.xhr;
  if (request.responseURL.endsWith("/token") === false) {
    return;
  }

  const spinner = getBtnSpinner();
  const btnText = getBtnText();

  spinner.classList.add("hidden");
  btnText.classList.remove("hidden");

  let message;
  switch (request.status) {
    case 403:
      message = "Invalid CSRF. Please reload the page and try again.";
      break;
    case 451:
      message = "You don't have the permissions to perform this action.";
      break;
    default:
      message = "Something went wrong. Please try again.";
  }

  const token = document.querySelector("#token");
  token.classList.add("input-error");

  const errorLabel = document.querySelector("#token-error");
  errorLabel.textContent = message;
  errorLabel.classList.remove("invisible");
});

document.body.addEventListener("htmx:beforeSend", (event) => {
  if (event.detail.pathInfo.requestPath.endsWith("/token") === false) {
    return;
  }

  const spinner = getBtnSpinner();
  const btnText = getBtnText();

  spinner.classList.remove("hidden");
  btnText.classList.add("hidden");
});

const clipboardCopy = () => {
  const token = document.querySelector("#token");

  token.select();
  token.setSelectionRange(0, 9999);

  navigator.clipboard.writeText(token.value);
};
