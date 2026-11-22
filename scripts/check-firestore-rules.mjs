// Run only inside: firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"
import assert from 'node:assert/strict';

const host = process.env.FIRESTORE_EMULATOR_HOST;
assert.ok(host && /^(127\.0\.0\.1|localhost):\d+$/.test(host), 'Use the local Firestore emulator only');
const base = `http://${host}/v1/projects/demo-levelmate/databases/(default)/documents`;
const id = `rules-${Date.now()}`;
const token = uid => {
  const encode = value => Buffer.from(JSON.stringify(value)).toString('base64url');
  const now = Math.floor(Date.now() / 1000);
  return `${encode({alg: 'none', typ: 'JWT'})}.${encode({iss: 'https://securetoken.google.com/demo-levelmate', aud: 'demo-levelmate', sub: uid, user_id: uid, iat: now, exp: now + 3600, firebase: {sign_in_provider: 'custom', identities: {}}})}.`;
};
const string = value => ({stringValue: value});
const fields = {
  ownerUid: string('owner'), memberUids: {arrayValue: {values: [string('owner'), string('reader')]}},
  name: string('Shelf'), description: string(''), deadline: {nullValue: null},
  materials: {arrayValue: {values: []}}, steps: {arrayValue: {values: []}}
};
async function call(uid, method, path, body) {
  const headers = {'content-type': 'application/json'};
  if (uid) headers.authorization = `Bearer ${token(uid)}`;
  const response = await fetch(base + path, {method, headers, body: body ? JSON.stringify(body) : undefined});
  return response;
}
async function commit(uid, changedFields = fields, deleteDocument = false) {
  const name = `projects/demo-levelmate/databases/(default)/documents/sharedProjects/${id}`;
  const writes = deleteDocument ? [{delete: name}] : [{update: {name, fields: changedFields}, updateTransforms: [{fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME'}]}];
  return call(uid, 'POST', ':commit', {writes});
}
assert.equal((await commit('owner')).status, 200, 'Owner may create');
assert.equal((await call('reader', 'GET', `/sharedProjects/${id}`)).status, 200, 'Member may read');
assert.equal((await call('outsider', 'GET', `/sharedProjects/${id}`)).status, 403, 'Outsider may not read');
assert.equal((await call(null, 'GET', `/sharedProjects/${id}`)).status, 403, 'Anonymous may not read');
assert.equal((await commit('reader')).status, 403, 'Reader may not write');
assert.equal((await commit('owner', {...fields, ownerUid: string('reader')})).status, 403, 'Owner cannot transfer ownership');
assert.equal((await commit('owner', {...fields, path: string('/private/photo.jpg')})).status, 403, 'Photo fields rejected');
assert.equal((await commit('owner', {...fields, name: string('x'.repeat(41))})).status, 403, 'Invalid name rejected');
assert.equal((await commit('reader', fields, true)).status, 403, 'Reader may not delete');
assert.equal((await commit('owner', fields, true)).status, 200, 'Owner may revoke access');
console.log('Firestore rules: 10 checks passed');
