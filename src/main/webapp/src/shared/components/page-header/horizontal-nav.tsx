import React from "react";
import { NavLink } from "react-router-dom";

export interface HorizontalNavProps {
  navItems: { title: string; path: string }[];
}

export const HorizontalNav: React.FC<HorizontalNavProps> = ({ navItems }) => {
  return (
    <div className="pf-c-tabs">
      <ul className="pf-c-tabs__list">
        {navItems.map((f, index) => (
          <NavLink
            key={index}
            to={f.path}
            className="pf-c-tabs__item"
            activeClassName="pf-m-current"
            exact
          >
            <li key={index} className="pf-c-tabs__item">
              <button className="pf-c-tabs__link">
                <span className="pf-c-tabs__item-text">{f.title}</span>
              </button>
            </li>
          </NavLink>
        ))}
      </ul>
    </div>
  );
};
