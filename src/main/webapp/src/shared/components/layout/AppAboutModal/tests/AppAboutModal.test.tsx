import React from "react";
import { shallow } from "enzyme";
import { AppAboutModal } from "../AppAboutModal";

it("AppAboutModal", () => {
  const wrapper = shallow(<AppAboutModal isOpen={true} onClose={jest.fn()} />);
  expect(wrapper).toMatchSnapshot();
});
