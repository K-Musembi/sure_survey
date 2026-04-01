export default function Card({ hover, className = '', children, ...props }) {
  return (
    <div
      className={`${hover ? 'card-hover' : 'card'} ${className}`}
      {...props}
    >
      {children}
    </div>
  )
}
