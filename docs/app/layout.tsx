import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "SumPDF",
  description:
    "Your all-in-one PDF tool. Effortlessly combine multiple PDF files and convert various documents or images to PDFs. Free and open-source.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
