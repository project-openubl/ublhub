import React from "react";
import { mount } from "enzyme";
import { Provider } from "react-redux";
import { applyMiddleware, createStore } from "redux";
import thunk from "redux-thunk";
import { rootReducer } from "./rootReducer";

export const mockStore = (initialStatus?: any) =>
  initialStatus
    ? createStore(rootReducer, initialStatus, applyMiddleware(thunk))
    : createStore(rootReducer, applyMiddleware(thunk));

export const mountWithRedux = (component: any, store: any = mockStore()) =>
  mount(<Provider store={store}>{component}</Provider>);
