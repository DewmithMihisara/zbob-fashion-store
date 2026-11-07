"use client";

import type { BlogPost } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert, SavedAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { CheckboxField, SelectField, TextArea, TextField } from "@/components/ui/Field";
import { useAction } from "@/hooks/useAction";
import { api } from "@/lib/api";

const TOPICS = ["Style Guide", "Fashion Tips", "Brand Stories"];

export function PostForm({ post, onSaved }: { post?: BlogPost; onSaved: (saved: BlogPost) => void }) {
  const [title, setTitle] = useState(post?.title ?? "");
  const [excerpt, setExcerpt] = useState(post?.excerpt ?? "");
  const [body, setBody] = useState(post?.body ?? "");
  const [topic, setTopic] = useState(post?.topic ?? TOPICS[0]!);
  const [readMinutes, setReadMinutes] = useState(String(post?.readMinutes ?? 5));
  const [published, setPublished] = useState(Boolean(post?.publishedAt));
  const [saved, setSaved] = useState(false);
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSaved(false);
    const json = { title, excerpt, body, topic, readMinutes: Number(readMinutes), published };
    await run(async () => {
      const result = post
        ? await api<BlogPost>(`/admin/blog/${post.id}`, { method: "PUT", json })
        : await api<BlogPost>("/admin/blog", { method: "POST", json });
      setSaved(true);
      onSaved(result);
    });
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-xl border border-line bg-paper p-5">
      <ErrorAlert message={error} />
      <SavedAlert message={saved ? "Saved." : null} />
      <TextField label="Title" required maxLength={200} value={title} onChange={(e) => setTitle(e.target.value)} error={fieldErrors.title} />
      <TextArea label="Short summary" required rows={2} maxLength={500} value={excerpt} onChange={(e) => setExcerpt(e.target.value)} error={fieldErrors.excerpt} />
      <TextArea
        label="Post"
        required
        rows={14}
        hint="Start a line with ## for a heading and - for a list item. Leave a blank line between paragraphs."
        value={body}
        onChange={(e) => setBody(e.target.value)}
        error={fieldErrors.body}
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <SelectField label="Topic" value={topic} onChange={(e) => setTopic(e.target.value)}>
          {TOPICS.map((value) => (
            <option key={value}>{value}</option>
          ))}
        </SelectField>
        <TextField label="Minutes to read" type="number" min={1} max={120} required value={readMinutes} onChange={(e) => setReadMinutes(e.target.value)} />
      </div>
      <CheckboxField label="Published (visible on the website)" checked={published} onChange={(e) => setPublished(e.target.checked)} />
      <div>
        <Button type="submit" disabled={busy}>
          {busy ? "Saving..." : post ? "Save changes" : "Create post"}
        </Button>
      </div>
    </form>
  );
}
