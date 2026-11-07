import { parsePostBody } from "@/lib/post-body";

export function PostBody({ body }: { body: string }) {
  return (
    <div className="flex flex-col gap-5 text-[18px] leading-[27px] font-medium text-muted">
      {parsePostBody(body).map((block, index) => {
        if (block.type === "heading") {
          return (
            <h2 key={index} className="mt-6 text-[34px] leading-[1.2] font-medium tracking-tight-3 text-ink first:mt-0">
              {block.text}
            </h2>
          );
        }
        if (block.type === "list") {
          return (
            <ul key={index} className="list-disc pl-6">
              {block.items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          );
        }
        return <p key={index}>{block.text}</p>;
      })}
    </div>
  );
}
