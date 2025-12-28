export default defineNuxtRouteMiddleware((to) => {
  if (to.path === '/login') return

  const authCookie = useCookie('auth_token')

  if (!authCookie.value) {
    return navigateTo('/login')
  }
})
