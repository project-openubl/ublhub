import { rest } from "msw";

import * as AppRest from "@app/api/rest";
import { Project } from "@app/api/models";

function generateRandomId(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

export const mockProjectArray: Project[] = [
  {
    id: 1,
    name: "Project 1",
    description: "Description for Project 1",
  },
];

export const handlers = [
  // Commented out to avoid conflict with the real API
  rest.get(AppRest.PROJECTS, (req, res, ctx) => {
    return res(ctx.json(mockProjectArray));
  }),
  rest.get(`${AppRest.PROJECTS}/:id`, (req, res, ctx) => {
    const { id } = req.params;
    const mockProject = mockProjectArray.find(
      (app) => app.id === parseInt(id as string, 10)
    );
    if (mockProject) {
      return res(ctx.json(mockProject));
    } else {
      return res(ctx.status(404), ctx.json({ message: "Project not found" }));
    }
  }),
  rest.post(AppRest.PROJECTS, async (req, res, ctx) => {
    const newProject: Project = await req.json();
    newProject.id = generateRandomId(1000, 9999);

    const existingProjectIndex = mockProjectArray.findIndex(
      (elem) => elem.id === newProject.id
    );

    if (existingProjectIndex !== -1) {
      mockProjectArray[existingProjectIndex] = newProject;
      return res(
        ctx.status(200),
        ctx.json({ message: "Project updated successfully" })
      );
    } else {
      mockProjectArray.push(newProject);
      return res(
        ctx.status(201),
        ctx.json({ message: "Project created successfully" })
      );
    }
  }),
  rest.put(`${AppRest.PROJECTS}/:id`, async (req, res, ctx) => {
    const { id } = req.params;
    const newProject = await req.json();

    const existingProjectIndex = mockProjectArray.findIndex(
      (elem) => `${elem.id}` === `${id}`
    );

    if (existingProjectIndex !== -1) {
      mockProjectArray[existingProjectIndex] = newProject;
      return res(ctx.status(204));
    } else {
      return res(ctx.status(404));
    }
  }),
  rest.delete(`${AppRest.PROJECTS}/:id`, async (req, res, ctx) => {
    const { id } = req.params;

    const existingProjectIndex = mockProjectArray.findIndex(
      (elem) => `${elem.id}` === `${id}`
    );

    if (existingProjectIndex !== -1) {
      mockProjectArray.splice(existingProjectIndex, 1);
      return res(ctx.status(200));
    } else {
      return res(ctx.status(404));
    }
  }),
];

export default handlers;
