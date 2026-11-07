import { TextField } from "@/components/ui/Field";

export interface AddressValues {
  recipient: string;
  line1: string;
  line2: string;
  city: string;
  region: string;
  postalCode: string;
  country: string;
  phone: string;
}

export const emptyAddress: AddressValues = { recipient: "", line1: "", line2: "", city: "", region: "", postalCode: "", country: "", phone: "" };

/** Optional fields go to the API as null rather than empty text. */
export function toAddressJson(a: AddressValues) {
  return { ...a, line2: a.line2 || null, region: a.region || null, phone: a.phone || null, country: a.country.toUpperCase() };
}

export function AddressFields({
  value,
  onChange,
  errors = {},
}: {
  value: AddressValues;
  onChange: (next: AddressValues) => void;
  errors?: Record<string, string>;
}) {
  const set = (key: keyof AddressValues) => (e: { target: { value: string } }) => onChange({ ...value, [key]: e.target.value });
  return (
    <div className="grid gap-3 sm:grid-cols-2">
      <TextField label="Recipient" required maxLength={120} value={value.recipient} onChange={set("recipient")} error={errors["address.recipient"] ?? errors.recipient} />
      <TextField label="Phone" maxLength={30} value={value.phone} onChange={set("phone")} />
      <TextField label="Address line 1" required maxLength={200} value={value.line1} onChange={set("line1")} error={errors["address.line1"] ?? errors.line1} />
      <TextField label="Address line 2" maxLength={200} value={value.line2} onChange={set("line2")} />
      <TextField label="City" required maxLength={100} value={value.city} onChange={set("city")} error={errors["address.city"] ?? errors.city} />
      <TextField label="Region" maxLength={100} value={value.region} onChange={set("region")} />
      <TextField label="Postal code" required maxLength={20} value={value.postalCode} onChange={set("postalCode")} error={errors["address.postalCode"] ?? errors.postalCode} />
      <TextField label="Country code" required maxLength={2} placeholder="LK" value={value.country} onChange={set("country")} error={errors["address.country"] ?? errors.country} />
    </div>
  );
}
