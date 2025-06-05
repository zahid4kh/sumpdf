import type { Metadata } from "next";
import "./globals.css";
import { Analytics } from "@vercel/analytics/next";

export const metadata: Metadata = {
  title: "SumPDF",
  description:
    "Your all-in-one PDF tool. Effortlessly combine multiple PDF files and convert SVG, PNG, JPG, JPEG, ODT, DOC, DOCX files to PDFs. Free and open-source.",
  icons: {
    icon: [
      {
        url: "/icons/sumpdf.ico",
        sizes: "any",
      },
      {
        url: "/icons/sumpdf.png",
        type: "image/png",
      },
    ],
  },
  manifest: "/manifest.json",
  applicationName: "SumPDF",
  keywords: [
    "PDF",
    "PDF merger",
    "PDF converter",
    "document tools",
    "open source",
    "free PDF tool",
    "combine PDF",
    "convert to PDF",
  ],
  authors: [{ name: "SumPDF" }],
  creator: "SumPDF",
  publisher: "SumPDF",
  formatDetection: {
    telephone: false,
    date: false,
    address: false,
    email: false,
    url: false,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        {children}
        <Analytics />
      </body>
    </html>
  );
}
