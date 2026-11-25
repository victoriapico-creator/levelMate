// Run only with the local Firestore emulator and the demo-levelmate project.
import assert from 'node:assert/strict';
const host = process.env.FIRESTORE_EMULATOR_HOST;
assert.ok(host && /^(127\.0\.0\.1|localhost):\d+$/.test(host), 'Use the local Firestore emulator only');
const base = `http://${host}/v1/projects/demo-levelmate/databases/(default)/documents`;
const localId = String(Date.now());
const id = `owner_${localId}`;
const token = uid => {
  const encode = value => Buffer.from(JSON.stringify(value)).toString('base64url');
  const now = Math.floor(Date.now() / 1000);
  return `${encode({alg:'none',typ:'JWT'})}.${encode({iss:'https://securetoken.google.com/demo-levelmate',aud:'demo-levelmate',sub:uid,user_id:uid,iat:now,exp:now+3600,firebase:{sign_in_provider:'custom',identities:{}}})}.`;
};
const string = value => ({stringValue:value});
const members = values => ({arrayValue:{values:values.map(string)}});
const fields = {ownerUid:string('owner'),memberUids:members(['owner','reader']),name:string('Shelf'),description:string(''),deadline:{nullValue:null},materials:{arrayValue:{values:[]}},steps:{arrayValue:{values:[]}}};
let checks = 0;
async function expect(response, status, message) {
  assert.equal(response.status,status,`${message}: ${response.status === status ? '' : await response.text()}`);
  checks++;
  return response;
}
async function call(uid, method, path, body) {
  const headers = {'content-type':'application/json'};
  if (uid) headers.authorization = `Bearer ${token(uid)}`;
  return fetch(base+path,{method,headers,body:body ? JSON.stringify(body) : undefined});
}
async function commit(uid, changedFields=fields, {documentId=id,remove=false,union=null}={}) {
  const name = `projects/demo-levelmate/databases/(default)/documents/sharedProjects/${documentId}`;
  const transforms = [{fieldPath:'updatedAt',setToServerValue:'REQUEST_TIME'}];
  const data = {...changedFields};
  if (union) {
    delete data.memberUids;
    transforms.unshift({fieldPath:'memberUids',appendMissingElements:{values:union.map(string)}});
  }
  const update = {update:{name,fields:data},updateTransforms:transforms};
  // Match Android set(..., SetOptions.merge()): transform fields must be
  // absent from the update mask so existing members survive arrayUnion.
  if (union) update.updateMask = {fieldPaths:Object.keys(data)};
  const writes = remove ? [{delete:name}] : [update];
  return call(uid,'POST',':commit',{writes});
}
await expect(await call('owner','GET',`/sharedProjects/${id}`),403,'Missing copies are not readable before creation');
await expect(await commit('owner',fields,{union:['owner','reader']}),200,'First publish works with arrayUnion and no previous read');
await expect(await call('reader','GET',`/sharedProjects/${id}`),200,'Participant may read');
await expect(await call('outsider','GET',`/sharedProjects/${id}`),403,'Outsider may not read');
await expect(await call(null,'GET',`/sharedProjects/${id}`),403,'Anonymous may not read');
await expect(await commit('reader'),403,'Reader may not update');
await expect(await commit('owner',{...fields,ownerUid:string('reader')}),403,'Ownership cannot be transferred');
await expect(await commit('owner',{...fields,path:string('/private/photo.jpg')}),403,'Photo paths are rejected');
await expect(await commit('owner',{...fields,name:string('x'.repeat(41))}),403,'Invalid name is rejected');
await expect(await commit('owner',{...fields,memberUids:members(['reader'])}),403,'Owner must remain a member');
await expect(await commit('owner',{...fields,memberUids:members(['owner','owner'])}),403,'Duplicate participants are rejected');
await expect(await commit('owner',{...fields,memberUids:{arrayValue:{values:[string('owner'),{integerValue:'1'}]}}}),403,'Participant identifiers must be strings');
await expect(await commit('outsider',{...fields,ownerUid:string('outsider'),memberUids:members(['outsider'])},{documentId:`owner_${BigInt(localId)+1n}`}),403,'Another person cannot occupy the owner document namespace');
await expect(await commit('owner',fields,{union:['owner','second-reader']}),200,'Owner may add another participant atomically');
const result = await (await expect(await call('owner','GET',`/sharedProjects/${id}`),200,'Owner may read after updating')).json();
assert.deepEqual(result.fields.memberUids.arrayValue.values.map(v=>v.stringValue).sort(),['owner','reader','second-reader']); checks++;
await expect(await commit('owner',fields,{union:Array.from({length:10},(_,i)=>`extra-${i}`)}),403,'Participant limit is enforced after arrayUnion');
const query = uid => ({structuredQuery:{from:[{collectionId:'sharedProjects'}],where:{fieldFilter:{field:{fieldPath:'memberUids'},op:'ARRAY_CONTAINS',value:string(uid)}}}});
await expect(await call('reader','POST',':runQuery',query('reader')),200,'Member-filtered listener query is permitted');
await expect(await call('outsider','POST',':runQuery',{structuredQuery:{from:[{collectionId:'sharedProjects'}]}}),403,'Unfiltered queries are forbidden');
await expect(await commit('reader',fields,{remove:true}),403,'Reader may not revoke access');
await expect(await commit('owner',fields,{remove:true}),200,'Owner may revoke access');
console.log(`Firestore rules: ${checks} checks passed`);
