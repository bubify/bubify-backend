const axios = require('axios');
axios.defaults.baseURL = "http://localhost:8900"

const sleep = async (ms) => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};

const cancelDemoRequest = async (token, helpId) => {
  axios.get(
    "/demonstration/cancel/" + helpId,
    {
      headers: {
        token: token,
      },
    }
  );
}

const cancelHelpRequest = async (token, demoId) => {
  axios.get(
    "/helpRequests/cancel/" + demoId,
    {
      headers: {
        token: token,
      },
    }
  );
}

const createDemonstrationRequestStudent = async () => {
  const u = Math.floor(Math.random() * 100);
  const suResponse = await axios.get("/su?username=" + "studentsim" + u);
  const token = suResponse.data;
  const loginResponse = await axios.get(
    "/basicData",
    {
      headers: {
        token: token,
      },
    }
  );

  const response = await axios.post("/demonstration/request", {
    ids: [loginResponse.data.user.id],
    achievementIds: [9]
  }, {
    headers: {
      token: token,
    },
  });

  const demoId = response.data.id;
  const timeToWait = Math.floor(Math.random() * 31) + 1;
  setTimeout(() => cancelDemoRequest(token, demoId), timeToWait * 1000);
}

const createHelpRequestStudent = async () => {
  const u = Math.floor(Math.random() * 100);
  const suResponse = await axios.get("/su?username=" + "studentsim" + u);
  const token = suResponse.data;
  const loginResponse = await axios.get(
    "/basicData",
    {
      headers: {
        token: token,
      },
    }
  );
  const response = await axios.post("/askForHelp", {
    ids: [loginResponse.data.user.id],
    message: "my random message!"
  }, {
    headers: {
      token: token,
    },
  });

  const helpId = response.data.id;

  const timeToWait = Math.floor(Math.random() * 31) + 1;
  setTimeout(() => cancelHelpRequest(token, helpId), timeToWait * 1000);
}

const runner = async () => {
  while (true) {
    createHelpRequestStudent();
    createDemonstrationRequestStudent();
    await sleep(1000);
  }
}
runner();