const Footer = () => {
  return (
    <footer className="bg-white border-t border-gray-100 py-6">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-sm text-gray-600">
        © {new Date().getFullYear()} Sure Survey — Built with care.
      </div>
    </footer>
  )
}

export default Footer