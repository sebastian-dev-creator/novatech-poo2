const {chromium}=require('C:/Users/gamep/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');fs.mkdirSync('outputs/cumplimiento',{recursive:true});
(async()=>{
 const browser=await chromium.launch({headless:true,channel:'msedge'});
 try{
  const page=await browser.newPage({viewport:{width:1440,height:1000}});
  const base='http://127.0.0.1:18081/novatech';
  await page.goto(base+'/login');await page.locator('#usuario').fill('demo_et4_admin');await page.locator('#password').fill('DemoLocalEtapa4!');await page.getByRole('button',{name:'Iniciar sesión'}).click();await page.waitForURL('**/menu');
  await page.getByRole('navigation',{name:'Navegación principal'}).getByRole('link',{name:'Usuarios',exact:false}).click();await page.waitForURL('**/usuarios');
  const row=page.locator('tbody tr').filter({hasText:'demo_et4_lucia'});await row.getByRole('link',{name:'Contactos',exact:true}).click();
  await page.locator('[name=valor]').fill('lucia@example.com');await page.locator('[name=tipo]').fill('personal');await page.getByRole('button',{name:'Guardar contacto'}).click();await page.waitForURL('**/contactos?usuarioId=*&clase=correo');
  assert.ok(await page.locator('tbody').getByText('lucia@example.com').isVisible());await page.screenshot({path:'outputs/cumplimiento/contactos.png',fullPage:true});
  await page.locator('tbody').getByRole('link',{name:'Editar'}).click();await page.locator('[name=tipo]').fill('laboral');await page.getByRole('button',{name:'Guardar contacto'}).click();await page.waitForURL('**/contactos?usuarioId=*&clase=correo');assert.ok(await page.locator('tbody').getByText('laboral').isVisible());
  await page.locator('tbody summary').click();await page.getByRole('button',{name:'Confirmar eliminación'}).click();await page.waitForURL('**/contactos?usuarioId=*&clase=correo');assert.equal(await page.locator('tbody').getByText('lucia@example.com').count(),0);
  await page.getByRole('link',{name:'Direcciones',exact:true}).click();await page.screenshot({path:'outputs/cumplimiento/direcciones.png',fullPage:true});
  for(const width of [390,320]){await page.setViewportSize({width,height:844});assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Contact form fits mobile');}
  await page.screenshot({path:'outputs/cumplimiento/direcciones-mobile.png',fullPage:true});
  await page.goto(base+'/ubicaciones?nivel=distrito');assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Location form fits mobile');await page.setViewportSize({width:1440,height:1000});await page.screenshot({path:'outputs/cumplimiento/ubicaciones.png',fullPage:true});
  await page.goto(base+'/roles');await page.locator('tbody tr').filter({hasText:'OPERADOR'}).getByRole('link',{name:'Permisos',exact:true}).click();assert.equal(await page.locator('input[name=permiso]').count(),4);await page.screenshot({path:'outputs/cumplimiento/permisos.png',fullPage:true});
  await page.setViewportSize({width:320,height:844});assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Permission form fits mobile');
  console.log('PASS: Command navigation, browser contact CRUD, address/location/permission forms, responsive 320/390px.');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
