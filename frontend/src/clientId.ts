const KEY = 'clientId'

// anonymous identity instead of login - stays in this browser
export function getClientId() {
  let id = localStorage.getItem(KEY)
  if (!id) {
    id = crypto.randomUUID()
    localStorage.setItem(KEY, id)
  }
  return id
}
