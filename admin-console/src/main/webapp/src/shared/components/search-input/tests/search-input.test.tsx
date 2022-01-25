import React from "react";
import { mount, shallow } from "enzyme";

import { SearchInput } from "../search-input";
import { Button } from "@patternfly/react-core";

describe("SearchInput", () => {
  it("Renders without crashing", () => {
    const wrapper = shallow(<SearchInput onSearch={jest.fn} />);
    expect(wrapper).toMatchSnapshot();
  });

  it("Callback when click on search button", () => {
    const callbackSpy = jest.fn();
    const wrapper = mount(<SearchInput onSearch={callbackSpy} />);

    const nameInput = wrapper.find('input[name="filterText"]');
    (nameInput.getDOMNode() as HTMLInputElement).value = "myCompany";
    nameInput.simulate("change");
    wrapper.update();

    wrapper.find(Button).simulate("click");
    expect(callbackSpy).toHaveBeenCalledTimes(1);
    expect(callbackSpy).toHaveBeenCalledWith("myCompany");
  });

  it("Callback when click on press Enter", () => {
    const callbackSpy = jest.fn();
    const wrapper = mount(<SearchInput onSearch={callbackSpy} />);

    const nameInput = wrapper.find('input[name="filterText"]');
    (nameInput.getDOMNode() as HTMLInputElement).value = "myCompany";
    nameInput.simulate("change");
    nameInput.simulate("keydown", { key: "Enter" });
    wrapper.update();

    expect(callbackSpy).toHaveBeenCalledTimes(1);
    expect(callbackSpy).toHaveBeenCalledWith("myCompany");
  });
});
