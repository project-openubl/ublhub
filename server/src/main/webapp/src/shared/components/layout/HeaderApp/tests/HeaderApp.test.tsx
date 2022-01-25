import React from "react";
import { shallow } from "enzyme";
import { HeaderApp } from "../HeaderApp";

it("Test snapshot", () => {
  const wrapper = shallow(<HeaderApp />);
  expect(wrapper).toMatchSnapshot();
});
