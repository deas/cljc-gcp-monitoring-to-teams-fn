// Borrowed from
// https://gist.github.com/jackrusher/1cc61e0ca0e929b9ec21bf4407af6d75

import { loadFile } from 'nbb';
const { gcp2teams } = await loadFile('./gcp_monitoring_to_teams.cljc');
export { gcp2teams }