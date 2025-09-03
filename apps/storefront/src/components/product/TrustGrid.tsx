import { CardIcon, PinIcon, ReturnIcon, ShieldIcon } from "@/components/ui/icons";

/** Placeholder wording: confirm against zbob's real delivery, payment and returns policies before launch. */
const ITEMS = [
  { icon: ShieldIcon, title: "Checked quality", text: "Every piece is inspected before it is packed and sent to you." },
  { icon: PinIcon, title: "Clear delivery", text: "Delivery details are confirmed with you when you place your order." },
  { icon: CardIcon, title: "Safe payment", text: "Secure online payment will be available when ordering opens." },
  { icon: ReturnIcon, title: "Simple returns", text: "If something is not right, contact us and we will help you sort it out." },
];

export function TrustGrid() {
  return (
    <div className="mt-[58px] grid gap-[14px] tablet:grid-cols-2 desktop:grid-cols-4">
      {ITEMS.map(({ icon: ItemIcon, title, text }) => (
        <div key={title} className="rounded-card bg-tile p-5">
          <span className="flex size-11 items-center justify-center rounded-full bg-paper">
            <ItemIcon size={18} />
          </span>
          <h2 className="mt-8 text-[20px] font-medium tracking-tight-3">{title}</h2>
          <p className="mt-2 text-[16px] leading-[24px] font-medium text-muted">{text}</p>
        </div>
      ))}
    </div>
  );
}
