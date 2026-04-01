import { useCallback, useState } from 'react'
import { HiOutlineCloudUpload } from 'react-icons/hi'

export default function FileDropzone({ accept = '.csv', onFile, label = 'Drop a file here or click to browse', className = '' }) {
  const [dragging, setDragging] = useState(false)
  const [fileName, setFileName] = useState('')

  const handleFile = useCallback((file) => {
    if (file) {
      setFileName(file.name)
      onFile?.(file)
    }
  }, [onFile])

  const handleDrop = useCallback((e) => {
    e.preventDefault()
    setDragging(false)
    const file = e.dataTransfer.files[0]
    handleFile(file)
  }, [handleFile])

  return (
    <label
      onDragOver={(e) => { e.preventDefault(); setDragging(true) }}
      onDragLeave={() => setDragging(false)}
      onDrop={handleDrop}
      className={`flex flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-8 cursor-pointer transition-colors
        ${dragging ? 'border-brand bg-brand/5' : 'border-[var(--border)] hover:border-[var(--text-muted)]'} ${className}`}
    >
      <HiOutlineCloudUpload className={`h-8 w-8 ${dragging ? 'text-brand' : 'text-[var(--text-muted)]'}`} />
      <span className="text-sm text-[var(--text-muted)]">{fileName || label}</span>
      <input
        type="file"
        accept={accept}
        className="hidden"
        onChange={(e) => handleFile(e.target.files[0])}
      />
    </label>
  )
}
