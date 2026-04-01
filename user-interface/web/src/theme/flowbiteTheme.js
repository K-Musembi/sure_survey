const flowbiteTheme = {
  button: {
    color: {
      brand: 'bg-brand text-white hover:bg-brand-light focus:ring-4 focus:ring-brand/30',
      light: 'bg-[var(--surface)] text-[var(--text)] border border-[var(--border)] hover:bg-[var(--surface-hover)]',
    },
    pill: {
      on: 'rounded-full',
    },
  },
  textInput: {
    field: {
      input: {
        base: 'input-field',
        colors: {
          gray: 'bg-[var(--surface)] border-[var(--border)] text-[var(--text)] focus:border-brand focus:ring-brand/30',
        },
      },
    },
  },
  modal: {
    root: {
      base: 'fixed inset-x-0 top-0 z-50 h-screen overflow-y-auto overflow-x-hidden md:inset-0 md:h-full',
    },
    content: {
      base: 'relative h-full w-full p-4 md:h-auto',
      inner: 'relative rounded-xl bg-[var(--surface)] shadow-xl border border-[var(--border)]',
    },
    header: {
      base: 'flex items-start justify-between rounded-t border-b border-[var(--border)] p-5',
      title: 'text-xl font-semibold text-[var(--text)]',
    },
    body: {
      base: 'p-6',
    },
  },
  dropdown: {
    floating: {
      base: 'z-10 w-fit rounded-xl divide-y divide-[var(--border)] shadow-lg border border-[var(--border)]',
      style: {
        auto: 'bg-[var(--surface)] text-[var(--text)]',
      },
      item: {
        base: 'flex items-center justify-start py-2 px-4 text-sm cursor-pointer hover:bg-[var(--surface-hover)]',
      },
    },
  },
  tooltip: {
    style: {
      auto: 'bg-[var(--surface)] text-[var(--text)] border border-[var(--border)]',
    },
  },
  sidebar: {
    root: {
      inner: 'h-full overflow-y-auto overflow-x-hidden bg-[var(--surface)] py-4 px-3',
    },
    item: {
      base: 'flex items-center justify-center rounded-lg p-2 text-[var(--text-muted)] hover:bg-[var(--surface-hover)] hover:text-[var(--text)]',
      active: 'bg-brand/10 text-brand',
    },
  },
  tab: {
    tablist: {
      tabitem: {
        base: 'flex items-center justify-center rounded-t-lg p-4 text-sm font-medium',
        styles: {
          underline: {
            active: {
              on: 'text-brand border-b-2 border-brand active',
              off: 'text-[var(--text-muted)] border-b-2 border-transparent hover:border-[var(--border)] hover:text-[var(--text)]',
            },
          },
        },
      },
    },
  },
}

export default flowbiteTheme
