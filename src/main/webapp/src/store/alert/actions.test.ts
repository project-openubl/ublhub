import { addAlert } from "./actions";

describe("Alert actions", () => {
  it("addAlert create actions", () => {
    const expectedAction = {
      type: "@@INSIGHTS-CORE/NOTIFICATIONS/ADD_NOTIFICATION",
      payload: {
        variant: "danger",
        title: "my title",
        description: "my message",
      },
    };

    const alertAction = addAlert("danger", "my title", "my message");
    expect(JSON.stringify(alertAction)).toBe(JSON.stringify(expectedAction));
  });
});
