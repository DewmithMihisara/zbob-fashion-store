export function ErrorAlert({ message }: { message: string | null | undefined }) {
  if (!message) {
    return null;
  }
  return (
    <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-[13px] text-danger">
      {message}
    </p>
  );
}

export function SavedAlert({ message }: { message: string | null | undefined }) {
  if (!message) {
    return null;
  }
  return (
    <p role="status" className="rounded-lg bg-green-50 px-3 py-2 text-[13px] text-success">
      {message}
    </p>
  );
}
