import { defineStore } from 'pinia'
import { useCookie, navigateTo } from '#app'

export const useAuthStore = defineStore('authStore', () => {
  const token = ref(null)
  const user = ref(null)

  function init() {
    const authCookie = useCookie('auth_token')
    if (authCookie.value) {
      token.value = authCookie.value
    }
  }

  function logout() {
    token.value = null
    user.value = null
    useCookie('auth_token').value = null
    navigateTo('/login')
  }

  return { token, user, init, logout }
})
