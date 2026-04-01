import useToastStore from '../stores/toastStore'

export default function useToast() {
  const { success, error, info, warning } = useToastStore()
  return { success, error, info, warning }
}
