import React from "react";
import { shallow } from "enzyme";
import { DefaultLayout } from "../DefaultLayout";

it("Test snapshot", () => {
  const wrapper = shallow(<DefaultLayout />);
  expect(wrapper).toMatchSnapshot();
});
