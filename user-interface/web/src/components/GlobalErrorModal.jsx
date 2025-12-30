import { Modal, Button, ModalHeader, ModalBody } from 'flowbite-react'
import { HiExclamationCircle } from 'react-icons/hi'
import useErrorStore from '../stores/errorStore'

const GlobalErrorModal = () => {
  const { isOpen, message, title, hideError } = useErrorStore()

  return (
    <Modal show={isOpen} onClose={hideError} size="md" popup>
      <ModalHeader />
      <ModalBody>
        <div className="text-center">
          <HiExclamationCircle className="mx-auto mb-4 h-14 w-14 text-red-600 dark:text-red-200" />
          <h3 className="mb-2 text-lg font-normal text-gray-900 dark:text-gray-400">
            {title}
          </h3>
          <p className="mb-5 text-sm text-gray-500 dark:text-gray-400">
            {message}
          </p>
          <div className="flex justify-center gap-4">
            <Button color="failure" onClick={hideError}>
              OK
            </Button>
          </div>
        </div>
      </ModalBody>
    </Modal>
  )
}

export default GlobalErrorModal