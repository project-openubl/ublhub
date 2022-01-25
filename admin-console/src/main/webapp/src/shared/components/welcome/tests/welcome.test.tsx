import React from "react";
import { shallow, mount } from "enzyme";
import { Button } from "@patternfly/react-core";
import { Welcome } from "../welcome";

describe("Welcome", () => {
  it("Renders without crashing", () => {
    const wrapper = shallow(<Welcome onPrimaryAction={jest.fn} />);
    expect(wrapper).toMatchSnapshot();
  });

  it("onPrimaryAction should call callback", () => {
    const mockCallback = jest.fn();

    const wrapper = mount(<Welcome onPrimaryAction={mockCallback} />);
    wrapper.find(Button).simulate("click");
    expect(mockCallback.mock.calls.length).toEqual(1);
  });
});
