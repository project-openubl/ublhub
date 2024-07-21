import { decodeEnv, buildOpenublEnv } from "@openubl-ui/common";

export const ENV = buildOpenublEnv(decodeEnv(window._env));

export default ENV;
