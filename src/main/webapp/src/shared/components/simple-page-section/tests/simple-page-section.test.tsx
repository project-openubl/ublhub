import React from "react";
import { shallow, mount } from "enzyme";
import { Text } from "@patternfly/react-core";
import { SimplePageSection } from "../simple-page-section";

describe("SimplePageSection", () => {
  it("Renders without crashing", () => {
    const wrapper = shallow(<SimplePageSection title="myTitle" />);
    expect(wrapper).toMatchSnapshot();
  });

  it("Renders without description", () => {
    const wrapper = mount(<SimplePageSection title="myTitle" />);

    expect(wrapper.find(Text).length).toEqual(1);
    expect(wrapper.find(Text).at(0).text()).toMatch(/myTitle/);
  });

  it("Renders with description", () => {
    const wrapper = mount(
      <SimplePageSection title="myTitle" description="myDescription" />
    );

    expect(wrapper.find(Text).length).toEqual(2);
    expect(wrapper.find(Text).at(0).text()).toMatch(/myTitle/);
    expect(wrapper.find(Text).at(1).text()).toMatch(/myDescription/);
  });
});
