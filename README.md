# Fdx-Mock-Auth-Server

## Description
This project helps banks/financial institutions (FIs) implement an auth server in their organization. While you can choose any auth server, OAuth2.0 or OIDC is preferred. All responses conform to the FDX 6.0 standard.

## Prerequisites
1. An IDE that supports Java 17 or above (e.g., Eclipse, IntelliJ, STS).
2. Java installed on your local system.
3. Postman installed on your local system.

## Installation Instructions
1. Download the project from GitHub.
2. Import the project into your preferred IDE as an existing Maven project.
3. Run the application as a Java Application.
4. Once the project is running, download the Postman collection from the resource/postman folder. 
5. Import the downloaded Postman collection into Postman. 
6. You can now use the imported collection to send requests to the API endpoints and observe the responses.

## Usage
**Note:** To run through consent journey and resource APIs skipping DCR, follow below steps with default client id: dh-fdx-client-registrar-2

[<img src="https://run.pstmn.io/button.svg" alt="Run In Postman" style="width: 128px; height: 32px;">](https://app.getpostman.com/run-collection/24674504-d6196631-b582-41a3-a161-30dbafb0027c?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D24674504-d6196631-b582-41a3-a161-30dbafb0027c%26entityType%3Dcollection%26workspaceId%3D76cee3a2-4532-4ca9-a9e6-f1c349dd1c95#?env%5BBank%20API%20Nexus%5D=W3sia2V5IjoiaG9zdCIsInZhbHVlIjoiaHR0cHM6Ly9mZHgtbW9jay1hdXRob3JpemF0aW9uLXNlcnZlci5kZXYuZmluaS5jaXR5IiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImRhdGFob2xkZXJEY3JBY2Nlc3NUb2tlbiIsInZhbHVlIjoiQmVhcmVyIGV5SnJhV1FpT2lJNU9XSmpNek5sWlMxaE1EUmtMVFJoT0RrdE9HRmxNQzAxWkdWaVpESXhORFZoWVdJaUxDSjBlWEFpT2lKaGRDdHFkM1FpTENKaGJHY2lPaUpRVXpJMU5pSjkuZXlKemRXSWlPaUprYUMxbVpIZ3RZMnhwWlc1MExYSmxaMmx6ZEhKaGNpMHlJaXdpWVhWa0lqb2laR2d0Wm1SNExXTnNhV1Z1ZEMxeVpXZHBjM1J5WVhJdE1pSXNJbTVpWmlJNk1UY3hOVGMyTmpZM01Td2ljMk52Y0dVaU9sc2lZMnhwWlc1MExuSmxZV1FpWFN3aWFYTnpJam9pYUhSMGNEb3ZMMnh2WTJGc2FHOXpkRG80TURnd0lpd2laWGh3SWpveE56RTFOelkyT1RjeExDSnBZWFFpT2pFM01UVTNOalkyTnpFc0ltcDBhU0k2SW1RME5UVTRNRGMxTFROak1UY3RORGRtTlMwNU0yUTRMVGs0TTJVeU5qQmlaVEJqTlNKOS5tMnpmRHRmcEo3UFg0d1VrQ3h6WWFpcWZFR3dFZW5pMVU3b3FaRUlzZmJSc3NQc0VTSFNyaFROM1BjaW4wNXozTHFGVmNidzJZemQyOE1QcWxvYTd5ckFPRUlDQjlTNUNRemk2dG81bGFMMm5iTVZ0bGphMVJVUTMzejk2NGhPYWdkajF6SUkzWGpWa1Y5a3ZFaWVKOV9RNFc2TTNJUWJzTjUxeUVLN1gtTFlWeDBITG9jbHl5b0JIWGNRdFdIZXZlaXZFQUh3X1FYUm9QZC04cnV1YTd3NHpmVXhseXhwa0hpUnJmNFdoVzlLSGdCalQwOW41eXlKRzhQX05WdnVhemZLN2FhTzFnM3RDZER2M1p3T1RlS1FNNlFVQnNibi1PYUxROGFPR3ltbUVzREpvZkxuc2w5d1dDRGV3SENOMVZTbWh6TkJSSlQ2bEUwNDFwdTh3dGciLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiY2xpZW50SWQiLCJ2YWx1ZSI6ImRoLWZkeC1jbGllbnQtcmVnaXN0cmFyLTIiLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiY2xpZW50U2VjcmV0IiwidmFsdWUiOiJzZWNyZXQiLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5Ijoib2F1dGgyX3Jlc3BvbnNlX3R5cGUiLCJ2YWx1ZSI6ImNvZGUiLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoicmVkaXJlY3RVcmwiLCJ2YWx1ZSI6Imh0dHBzOi8vb2F1dGgucHN0bW4uaW8vdjEvYnJvd3Nlci1jYWxsYmFjayIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJkckp3dFNlY3JldCIsInZhbHVlIjoiLS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLVxuTUlJRW93SUJBQUtDQVFFQXNKK0JKOC84S1pGN1NCMkZ1YmIzaGh2NXU2N1BNMTIycUVpUDFKVDBST2xRWDZrYlxuT1pGcEszWHRpZitlZHFtMzczYnVzejhDMEFZazVZZ29rVXFmZlNQTEFCcnVqNHRwU3hBNzFmbjVGbnBJSWZZNlxucHFCZGg1RCtJb3ZnNHl2UzdjVi9vZXJrc210cnlRNnJQd01KQUV4Q1E0M1BhL2lpWmxjT3A0Q0plNk5DcjBTM1xuei91aldtNER2V1E0RDNTV1FJV3FGMTFYdXJhZFlpQ1Fab1FNZ2xJcGZHa0VGM21PMHpqUDBkUzlwZTNMbEkwWVxuTDJXSW5OYjNSMHMxVUxSL2crdU1VT0p2OTBCc0Q4K3JYdWtLZmZnUlJqWmJlcStsWjg3MlFEL0Qva3lSb3BDcVxueGR5bUV3Tzl4MVB1R2ozU2gwMGIrYUc1RWpMamM5L2JQVlF0QlFJREFRQUJBb0lCQUJPMWFNTVppVWpXTEF5TFxuQnM0SFphMTRWN0NpdkFvK1RkQ1BjRVZIYmM1MnZubytNNFNEL2xsVUtuSjRyS1VQSVRBK2REcGFNS1VuMmx3bVxuaW1oVURHSDVhWUNVZ01SUnlTa1FJYTBwM0txdWNIZ2hzcisvSzFDaVZFbGtsRzZsQ0Q5eUIyRDVCaFlHdUp0UlxuNExnQ1RiSFQwSjBiS09jaWNuUzdDQzZXd0VDa2NwVit1UTd3eTZPQ0JsK2QwUGhJZnZ5WmhGaksvMjlDNy82TVxuOGh4VjhucDk0WFhFdDVQQ2l1aGcvQVZYSmpJTWRhdHRpY3FEeHB2eDRVSzZwMGtxeWtaZG95ZGFFcHlGaFpIS1xuRFExWm1kelcwVlBwSkVWcnBIVHg1Q0M0ZTJpYU5zMDRJY2duekM4Vm1HVEt4T3JTQjViancwcjJVZFpxTG5OOVxuQkF0cDkyMENnWUVBNFp6K3lBQnViU0UvZklyRGc2QU9Oc1pVQS9xOTR5cjBHa3UrWlU2QkVZSlF6K3lMc1lmNVxuRFhmMDJRd2NYREYxVWNOZngwVGhjMFgyL002cEFTSFhTbGIrQVh0dy95bTJLOWpwZzl0bUM2Z1g0WGdNTjY4Q1xuV2lYMDBoOGtZRHp4Q1ZYbThwREk4aWxHYTQ3UFc4aTNsKy9rT1VuY2p1YlZiMS9PamVYNnVkY0NnWUVBeUdsYlxubGhFV2x6Yjg5cXQvVXN5c3k3OTlEZEwrTEl0NTNZYTRabjFJVEpXQkJOdi9LVTU5eVZUMzJMbkc5YUVMcHMzUFxuSkZEK2Z0dVE3V1NDRVZLK0pFd05LM0UvWWhSYVV3QjZUbEpPSVBOM2xSOWx2N3AwK1M2b0ZHeld4ZWpia2g3N1xuZFhtOUh1VmZHbFpmVmM2algra3I5K2p5NDhnRVZSd0lXMTBYRElNQ2dZQi9IWktibzBxVWFDQTZEb0Q4SDJrQ1xueVptNnFFTG14R1BHa1JpVVFiZ000bXlmeWZvbks2SlNTVjBHRVlIRkhBU3E3YmFBTXJvSDFjckVRN2dMTUVMNVxuMnF2QXNBbEphdm4rR0hmeFYrTlYrLzZBaGIvMGU4R0tnMG8rUFZla2RaR2xIZVpDa3ZyMUhmWlVVYUZaYUd5d1xueVhQQUozS0NmV0tTSHR2bExVZW4vd0tCZ1FDQms0VXd5TW1uYVZrekQzMGFCdFd1UXBmVFNWa25hZUtZTlZPaVxuaUZ3bmovNUVLdzJXSW5hM3BxVTh6aGp5bFhhWUdiZFZUcnpOUHA4OHJlQmUzUFd0T0RMSHBNZ0xYUWR0WlFDdlxuQlE0WXhmSEtTK3VZaERDL0w4aGl0R0JFdzZLN25pYU5OUEh2T1BROWJoemp1Ti91MGk2b2NrZ2d1Q0Q4K2t2NFxuN241Sml3S0JnRHdaTXM3cFhwNnExaWhVaGZpTWQ0Q1pnRklpblE5ZitwV05sd3RLZEpHUmFHQkFWanhtS2c2MFxuREhNODRjMXRhU0tCaFIvZ2EzYzdISTlHT09jNGJ3Y0t4NGxDZFFkWTVSckZFYUVCdDRZVW0ySDdBZ1JjZDIxa1xuYzNmMk9PbDdwUUJ5bEhMVUEyQ0dHWUdXWjMvTUVDRVVoTVpBY21xQzFWYVZ5dm5CNEVqNlxuLS0tLS1FTkQgUlNBIFBSSVZBVEUgS0VZLS0tLS0iLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiZGF0YWhvbGRlcklzc3VlciIsInZhbHVlIjoiaHR0cHM6Ly9mZHgtbW9jay1hdXRob3JpemF0aW9uLXNlcnZlci5kZXYuZmluaS5jaXR5IiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImRhdGFob2xkZXJBY2Nlc3NTaGFyaW5nRHVyYXRpb24iLCJ2YWx1ZSI6IjMxNTM2MDAwIiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImRhdGFob2xkZXJBY2Nlc3NEdXJhdGlvbiIsInZhbHVlIjoiMzAwIiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6Im9hdXRoMl9yZXNwb25zZV9tb2RlIiwidmFsdWUiOiJxdWVyeSIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJwa2NlX2NvZGVfY2hhbGxlbmdlIiwidmFsdWUiOiJuWllaLVVPZlFjMk5zV1Y3TmtrbGwxOUFMTTZtcmNDN2VYVldVTVBZcFFRIiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImRhdGFob2xkZXJBdXRob3Jpc2F0aW9uUmVxdWVzdEp3dCIsInZhbHVlIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKUVV6STFOaUo5LmV5SnBZWFFpT2pFM01UVXpNak01TmpVc0ltNWlaaUk2TVRjeE5UTXlNemsyTVN3aVpYaHdJam94TnpFMU16STBNalkxTENKcWRHa2lPaUl6WW1FNE9EWmxOQzFsTm1RM0xUUm1OR1l0WVRrek5TMDROREV3TlRreU1HVmlOalVpTENKcGMzTWlPaUprYUMxbVpIZ3RZMnhwWlc1MExYSmxaMmx6ZEhKaGNpMHhJaXdpWVhWa0lqb2liRzlqWVd4b2IzTjBPamd3T0RBaUxDSnlaWE53YjI1elpWOTBlWEJsSWpvaVkyOWtaU0lzSW1Oc2FXVnVkRjlwWkNJNkltUm9MV1prZUMxamJHbGxiblF0Y21WbmFYTjBjbUZ5TFRFaUxDSnlaV1JwY21WamRGOTFjbWtpT2lKb2RIUndjem92TDI5aGRYUm9MbkJ6ZEcxdUxtbHZMM1l4TDJOaGJHeGlZV05ySWl3aWNtVnpjRzl1YzJWZmJXOWtaU0k2SW5GMVpYSjVJaXdpYzJOdmNHVWlPaUptWkhnNmRISmhibk5oWTNScGIyNXpPbkpsWVdRZ1ptUjRPbUZqWTI5MWJuUmlZWE5wWXpweVpXRmtJRzl3Wlc1cFpDQm1aSGc2WTNWemRHOXRaWEp3WlhKemIyNWhiRHB5WldGa0lHWmtlRHBoWTJOdmRXNTBaR1YwWVdsc1pXUTZjbVZoWkNCbVpIZzZhVzUyWlhOMGJXVnVkSE02Y21WaFpDQm1aSGc2Y0dGNWJXVnVkSE4xY0hCdmNuUTZjbVZoWkNCbVpIZzZZV05qYjNWdWRIQmhlVzFsYm5Sek9uSmxZV1FnWm1SNE9tSnBiR3h6T25KbFlXUWdabVI0T21sdFlXZGxjenB5WldGa0lHWmtlRHB5WlhkaGNtUnpPbkpsWVdRZ1ptUjRPblJoZURweVpXRmtJR1prZURwemRHRjBaVzFsYm5Sek9uSmxZV1FnWm1SNE9tTjFjM1J2YldWeVkyOXVkR0ZqZERweVpXRmtJaXdpYzNSaGRHVWlPaUowWlhOMGMzUmhkR1VpTENKdWIyNWpaU0k2SW1JeFlUbGxNbVl5TFRNME16Y3RORE16WXkxaVl6ZGlMV0ZsWVdNMVpESm1OREppTnlJc0ltTnZaR1ZmWTJoaGJHeGxibWRsSWpvaWJscFpXaTFWVDJaUll6Sk9jMWRXTjA1cmEyeHNNVGxCVEUwMmJYSmpRemRsV0ZaWFZVMVFXWEJSVVZ4dUlpd2lZMjlrWlY5amFHRnNiR1Z1WjJWZmJXVjBhRzlrSWpvaVV6STFOaUo5Lk0zUzRpWjl2ZFdOR1NDb0hvUldUSlRTY2JSNzdhNWVwR2hveDMwMzJMNXNsSjZCWW9YbFB1OU9tLWoxMjJCcTZScnI5dTloVDgwWjQwR3g5OW1nYmVXM2ZQQVNaN3d3anR5SkhHRmluVjJKYUVZczNpQ002R2NSdmhVc1NTdk1PZWl6b0dYd1hVMEFKV1VmdU5EalVwOHFVSVR2V0l2SFNoVjNJREtURDFwMlk1Y21vOWFrb0VBTFNGaFlpYU1rWG5LaGdvUmsxcWpyWU5IeTgtemtGUUhLM3VFVmdkREhZUHVqVEs1SE1kVW5tSWZuNVBpMHNoNWZRWF9VYTZycUdnZXIwT2NyMEdIektXenFQaEJVdk5VZ2wwYTVyX2s2N2FJeDJ0bGl0cHlNYktUVWhkdmhIRERWMDB0b0c5RDdXSGVWMFZwV2J4d0M4eEJzdUxJaEhDdyIsInR5cGUiOiJhbnkiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImFkckF1dGhTdGF0ZSIsInZhbHVlIjoidGVzdHN0YXRlIiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6InBrY2VfY29kZV92ZXJpZmllciIsInZhbHVlIjoiZmE1ODkzNmZmOWMxM2E4MjM3NWY0YzRjYmQ5YzdjYjE1MmIwODJiMTExMmI1NTI0MmM0MGJhYjkiLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiZGF0YWhvbGRlclBBUkp3dCIsInZhbHVlIjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKUVV6STFOaUo5LmV5SnBZWFFpT2pFM01UVTVNalE1TWpJc0ltNWlaaUk2TVRjeE5Ua3lORGt4T0N3aVpYaHdJam94TnpFMU9USTFNakl5TENKcWRHa2lPaUl6TURabFlXVTJNaTA0WkROa0xUUXlOR0V0WVRVeFpDMHdNV1kwTURCak9XRXlPVFFpTENKcGMzTWlPaUprYUMxbVpIZ3RZMnhwWlc1MExYSmxaMmx6ZEhKaGNpMHlJaXdpWVhWa0lqb2lhSFIwY0RvdkwyeHZZMkZzYUc5emREbzRNRGd3SWl3aWNtVnpjRzl1YzJWZmRIbHdaU0k2SW1OdlpHVWlMQ0pqYkdsbGJuUmZhV1FpT2lKa2FDMW1aSGd0WTJ4cFpXNTBMWEpsWjJsemRISmhjaTB5SWl3aWNtVmthWEpsWTNSZmRYSnBJam9pYUhSMGNITTZMeTl2WVhWMGFDNXdjM1J0Ymk1cGJ5OTJNUzlpY205M2MyVnlMV05oYkd4aVlXTnJJaXdpY21WemNHOXVjMlZmYlc5a1pTSTZJbkYxWlhKNUlpd2ljMk52Y0dVaU9pSm1aSGc2ZEhKaGJuTmhZM1JwYjI1ek9uSmxZV1FnWm1SNE9tRmpZMjkxYm5SaVlYTnBZenB5WldGa0lHOXdaVzVwWkNCbVpIZzZZM1Z6ZEc5dFpYSndaWEp6YjI1aGJEcHlaV0ZrSUdaa2VEcGhZMk52ZFc1MFpHVjBZV2xzWldRNmNtVmhaQ0JtWkhnNmFXNTJaWE4wYldWdWRITTZjbVZoWkNCbVpIZzZjR0Y1YldWdWRITjFjSEJ2Y25RNmNtVmhaQ0JtWkhnNllXTmpiM1Z1ZEhCaGVXMWxiblJ6T25KbFlXUWdabVI0T21KcGJHeHpPbkpsWVdRZ1ptUjRPbWx0WVdkbGN6cHlaV0ZrSUdaa2VEcHlaWGRoY21Sek9uSmxZV1FnWm1SNE9uUmhlRHB5WldGa0lHWmtlRHB6ZEdGMFpXMWxiblJ6T25KbFlXUWdabVI0T21OMWMzUnZiV1Z5WTI5dWRHRmpkRHB5WldGa0lpd2ljM1JoZEdVaU9pSjBaWE4wYzNSaGRHVWlMQ0p1YjI1alpTSTZJbU5qWWpWaVpERTVMVFl3TUdJdE5HVXdaaTA0TVdZeExUQmlaakZpT1RJelpEa3hNU0lzSW1OdlpHVmZZMmhoYkd4bGJtZGxJam9pYmxwWldpMVZUMlpSWXpKT2MxZFdOMDVyYTJ4c01UbEJURTAyYlhKalF6ZGxXRlpYVlUxUVdYQlJVU0lzSW1OdlpHVmZZMmhoYkd4bGJtZGxYMjFsZEdodlpDSTZJbE15TlRZaWZRLlBzUWhwYk5oeHEtUmp0NXEwbkxGZzlnWTZNT1ZtcWoxV1JtNlJObkRzOFNZX1Y2cHdUUWh4TndOdU9jTnItTVJqYUpvUDdpLXFVMFNJR1FIdWNPNFlsendPYjJXdk1CZnI3QWhuZVlDdzY4clhJRnR0UW1GbTNiQV9CM1FfeWpObmdFSm80QllZWHNWVWhDMEhSOThFeTgxWHI1SnhOMXpfM3daNlh4am5UQ0ZyWDRXS0hUSHR2dVMzaVVQTUJaT0p0ZGRNZmZ2TEpuUkN6aTR1aUw5RlJTOThqaWhhUDZveldvSFBYdzhkZV9ZYllJbnF1VlRmZHUzdXl4RTRKMzFaOU1zalc4YXRiWWg0TE4zdHlTRGN2UEZyTlkyUThFMktXUjBBM205TndLQnFVNEFTekZNRW8tTko2RXJvNERqU1RKTVhyVS1jcGUwS0k1elR4TVZ3USIsInR5cGUiOiJhbnkiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImRhdGFob2xkZXJQQVJBc3NlcnRpb25Kd3QiLCJ2YWx1ZSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSlFVekkxTmlKOS5leUpwWVhRaU9qRTNNVFU1TWpRNU1qSXNJbTVpWmlJNk1UY3hOVGt5TkRreE9Td2laWGh3SWpveE56RTFPVEkzT1RJeUxDSnFkR2tpT2lJd1l6UTJNREl3TVMxbU5tUTVMVFJoWlRndE9EZ3lOaTB6TjJNeFptVTNORE5tTURRaUxDSnBjM01pT2lKa2FDMW1aSGd0WTJ4cFpXNTBMWEpsWjJsemRISmhjaTB5SWl3aWMzVmlJam9pWkdndFptUjRMV05zYVdWdWRDMXlaV2RwYzNSeVlYSXRNaUlzSW1GMVpDSTZJbWgwZEhBNkx5OXNiMk5oYkdodmMzUTZPREE0TUNKOS5nTklYaWY0Mk9nZm9Ob3V4WWd1X3FVQ1dDUFhNQVcwX1hkTl9EeTJReVp1U2w1WVJQSXdGX3NSXzBsLU4xdXBEYjRkaXIwQ19xc01FbWRMNTd3MmhqUjJHUXo5eGVKLW92Sk8wZmRDZ2piV3pQY29BR0xXTG1HZUxsSll0SWhBd0lNdVZmRG51bHMyeWdsbXNwSzVUeG05OElqTjlDVEN1elp3MWgzaU1PV3FGT0dIQkJOOWpkYXFBcW11a2d2bWppN2hOdEc1U0E1d3hzWHlGUEdlRE5TZG1JRXNHT1h6cTkzZ1lfWXgycWJ0ODJNV1lxNHRoV1FkaXNNS2hPX05XYWFmbzdyNUlMTXl0ZmNCTFo4VEp2ZU1YR2xOUmRZOUZjbmYxNmw2YWlpSnEwNl82TWloRWpmVG9INkI3VjJpNmNWNnR3V1pnU3k2aloyc1VkZjlLbVEiLCJ0eXBlIjoiYW55IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJkaFBBUkluaXRpYXRlUmVzcFJlcXVlc3RVcmkiLCJ2YWx1ZSI6InVybjppZXRmOnBhcmFtczpvYXV0aDI6NjFmYTRjOTYtM2Y3Ni00MjJlLTgzYjQtMDBlNGJhZDk4NTQ5IiwidHlwZSI6ImFueSIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiZGhQQVJJbml0aWF0ZVJlc3BFeHBpcmVzSW4iLCJ2YWx1ZSI6OTAsInR5cGUiOiJhbnkiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImRhdGFob2xkZXJBdXRob3JpemVVUmwiLCJ2YWx1ZSI6ImxvY2FsaG9zdDo4MDgwL29hdXRoMi9hdXRob3JpemUiLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5Ijoic2NvcGUiLCJ2YWx1ZSI6ImZkeDp0cmFuc2FjdGlvbnM6cmVhZCUyMGZkeDphY2NvdW50YmFzaWM6cmVhZCUyMG9wZW5pZCUyMGZkeDpjdXN0b21lcnBlcnNvbmFsOnJlYWQlMjBmZHg6YWNjb3VudGRldGFpbGVkOnJlYWQlMjBmZHg6aW52ZXN0bWVudHM6cmVhZCUyMGZkeDpwYXltZW50c3VwcG9ydDpyZWFkJTIwZmR4OmFjY291bnRwYXltZW50czpyZWFkJTIwZmR4OmJpbGxzOnJlYWQlMjBmZHg6aW1hZ2VzOnJlYWQlMjBmZHg6cmV3YXJkczpyZWFkJTIwZmR4OnRheDpyZWFkJTIwZmR4OnN0YXRlbWVudHM6cmVhZCUyMGZkeDpjdXN0b21lcmNvbnRhY3Q6cmVhZCIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJyZWZyZXNoVG9rZW4iLCJ2YWx1ZSI6Ik9raTRKMzZwVWhyclF2ZExPeThfSXFYU2RFTHN6bXAyVmJaMGJOSnFBb0xTRlhJbjYzM1NqbHRWZ3hRb1RaODRiV1hub202NkFrUkZkdFZyOEhPcWkzeWFZeENaLTZDd3RkcGFuaVJOZTAweWdDRl9vOTFXT1cwS3k2dXBvWldKIiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImFjY2Vzc1Rva2VuIiwidmFsdWUiOiJleUpyYVdRaU9pSTVPV0pqTXpObFpTMWhNRFJrTFRSaE9Ea3RPR0ZsTUMwMVpHVmlaREl4TkRWaFlXSWlMQ0owZVhBaU9pSmhkQ3RxZDNRaUxDSmhiR2NpT2lKUVV6STFOaUo5LmV5SnpkV0lpT2lKbVpIaDFjMlZ5SWl3aVlYVmtJam9pWkdndFptUjRMV05zYVdWdWRDMXlaV2RwYzNSeVlYSXRNaUlzSW01aVppSTZNVGN4TlRreU5qYzNNaXdpWVdOamIzVnVkRjlwWkNJNld5SXlNREF3TVNJc0lqRXdNREF4SWl3aU1UQXdNRElpTENJek56UTJNamN5SWl3aU5UTXlOamc0TkNKZExDSnpZMjl3WlNJNld5Sm1aSGc2ZEhKaGJuTmhZM1JwYjI1ek9uSmxZV1FpTENKbVpIZzZZV05qYjNWdWRHSmhjMmxqT25KbFlXUWlMQ0p2Y0dWdWFXUWlMQ0ptWkhnNlkzVnpkRzl0WlhKd1pYSnpiMjVoYkRweVpXRmtJaXdpWm1SNE9tRmpZMjkxYm5Sa1pYUmhhV3hsWkRweVpXRmtJaXdpWm1SNE9tbHVkbVZ6ZEcxbGJuUnpPbkpsWVdRaUxDSm1aSGc2Y0dGNWJXVnVkSE4xY0hCdmNuUTZjbVZoWkNJc0ltWmtlRHBoWTJOdmRXNTBjR0Y1YldWdWRITTZjbVZoWkNJc0ltWmtlRHBpYVd4c2N6cHlaV0ZrSWl3aVptUjRPbWx0WVdkbGN6cHlaV0ZrSWl3aVptUjRPbkpsZDJGeVpITTZjbVZoWkNJc0ltWmtlRHAwWVhnNmNtVmhaQ0lzSW1aa2VEcHpkR0YwWlcxbGJuUnpPbkpsWVdRaUxDSm1aSGc2WTNWemRHOXRaWEpqYjI1MFlXTjBPbkpsWVdRaVhTd2lhWE56SWpvaWFIUjBjRG92TDJ4dlkyRnNhRzl6ZERvNE1EZ3dJaXdpWlhod0lqb3hOekUxT1RJM01EY3lMQ0pwWVhRaU9qRTNNVFU1TWpZM056SXNJbXAwYVNJNkltWTJNekV5WVRZeExXWTJZekF0TkdFMk5pMDRNREJrTFdWak1XVTNZVE0wTVdWak5pSXNJbVprZUVOdmJuTmxiblJKWkNJNklqSmxNemszWXpnd0xUQXpPVEV0TkdWbU9TMWlaVGs1TFdJNVpUQTVaR1psTjJGaVlTSXNJbk5vWVhKcGJtZGZaWGh3YVhKbGMxOWhkQ0k2TVRjME56UTJNVEF5T1gwLnBvLXh1ZS1nVW1TQXVpd1FLR2wtSFNJS0plUWdzQWw4YUVHVVgtZkF4S3dkS1FOLXFyTGR4UjlFSEw2dzIxSVlMcmhQTk9DVWdQUml1Z1MtNC1jWWx3NHdSNlZBbDhvN1BDcmtNV1R3MmZReE1TSUF4Y0Q5elh1MFpGLWhlSkdnZ3FINXEwWW0zWnlIcmxnai12UnZ0TExsN2JxNFFsX1A1VDdJSG1xTU02TVROVjItV3haUExXMWpjX1JHQzdDaVhGSlFMXy1nVlJUY0p1bW5LTUlZYTBRWUFkYldKRjU1bE15MFlINlFWUkFPSXQ3TW00TzJzVmlmUGZtRldCUmE1UXU1UU1zRnZIM1ZzeW1RckpyUURkUWltZTRNN1VSMXlWbnFQR3NKanN6djNKQkR5aGRsb3U5bzhYVzFsVkRETEZBWS1sUjZVZTZhMlFGZEI1aDlBdyIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJkYXRhaG9sZGVyQVRBc3NlcnRpb25Kd3QiLCJ2YWx1ZSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSlFVekkxTmlKOS5leUpwWVhRaU9qRTNNVGMyTlRVMk56QXNJbTVpWmlJNk1UY3hOelkxTlRZMk5pd2laWGh3SWpveE56RTNOalU0Tmpjd0xDSnFkR2tpT2lJd01UZzVNbVZrWmkxbE1ETmxMVFE1T1RjdE9EY3pNQzAyT0dRM1lXVTRNbVk1TmpVaUxDSnBjM01pT2lJMlUzaEdOamRNWVRaSmJIVmtXa3QyVkhaQlR6QmtOREkwTVhaeU5rRXlUVzluVlU1NFQwUnlVMkU0SWl3aWMzVmlJam9pTmxONFJqWTNUR0UyU1d4MVpGcExkbFIyUVU4d1pEUXlOREYyY2paQk1rMXZaMVZPZUU5RWNsTmhPQ0lzSW1GMVpDSTZJbWgwZEhBNkx5OXNiMk5oYkdodmMzUTZPREE0TUNKOS5mM1NWTU02QXdvcDRtb0hrX2pGYkhkSVhkbjY2THJ6S2Z0bnVjNzFaMndldzZwTFdJbGtxbFgzU1dDYmhRSjE5VndIYmlLUzJXWUtfbUZTVzY5WlVweEg4dVo3ZnBnNk01cUpOLWE1Qk10VGViTDhybzEtMUdTWUxhWm90TFBDalB6MDYtaURGVVVqU0xRelR6eEZKaUxoWVk4SUFoTEEzWHI3V2pBd0NTal9TM3dhQ05maE5LOTVULU9mbk1fMVd5NmIyMnhfZ3g2d2NPcGlrOVUxOVhoQTdoSzhSX0RLTWJDOUlneElYdFhrbnUweldsQjAzTzJyb3lOdVg2cHZzaUJOU0FhM3JDU3hUcVg0azFzc25nZEtPNTVpczBQZGloUjlZX0VkZFVuWTFSWjctWHRRNl9LWjQyNXRUM09nY01PV0V4NTJ3M2gtaThEUDBTWnVBbnciLCJ0eXBlIjoiYW55IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJkYXRhaG9sZGVyUkFUQXNzZXJ0aW9uSnd0IiwidmFsdWUiOiJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpRVXpJMU5pSjkuZXlKcFlYUWlPakUzTVRVNU1qWTNOekFzSW01aVppSTZNVGN4TlRreU5qYzJOaXdpWlhod0lqb3hOekUxT1RJNU56Y3dMQ0pxZEdraU9pSmpaR1F6T1RNNU5pMHpNRGc0TFRRNE1tSXRZVEk0TkMwM09EWTFZamsyWW1GbE0yUWlMQ0pwYzNNaU9pSmthQzFtWkhndFkyeHBaVzUwTFhKbFoybHpkSEpoY2kweUlpd2ljM1ZpSWpvaVpHZ3RabVI0TFdOc2FXVnVkQzF5WldkcGMzUnlZWEl0TWlJc0ltRjFaQ0k2SW1oMGRIQTZMeTlzYjJOaGJHaHZjM1E2T0RBNE1DSjkuTVVnNTBHY3JRcFhSOU5RUDlabjJpT095T2x5Zl9NM21hYjBZcmdWYXdTUDVBRDZERWRvZlM3TmFUYVh2QkdtcGpQcndNZ25Da0NBa19EaWNpeEpyS0RHZkhKZVlLbmNvakdzQURTa1k5M1NGSzJhTVlQbWJMeGR5aVV6NmUxZ1Zpb0M4bDlob01reWlFYXdsWUY4d0RfTkFNenRDS1FfNjRtTW1PUW51SkFLZHRxeTFUdllZLWpsbHlHLWF5S3Y4NWswdW9GdU1CUDhucTNyT250Qjg3NFdRelNiSFdKal9LaF9LdUhPNlVWS2RWTVNaZnM3U0R1azlwdGlsLUdJT2c3WnJlQlRlbTJQVWlDZFRpMHprOE4yTXpiUW1ubW5VcEo4SkkxUGNTS2oyZ0s5YTlRdEFyM0sydEk0dE8xWk92eGJJWHVkXzlRSmZ1ZmZrNmhya25nIiwidHlwZSI6ImFueSIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoicmVnaXN0cmF0aW9uX2FjY2Vzc190b2tlbiIsInZhbHVlIjoiIiwidHlwZSI6ImFueSIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoicmVzb3VyY2VfaG9zdCIsInZhbHVlIjoiaHR0cHM6Ly9mZHgtbW9jay1yZXNvdXJjZS1zZXJ2ZXIuZGV2LmZpbmkuY2l0eSIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJhdXRob3JpemVfdG9rZW4iLCJ2YWx1ZSI6IiIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJkZXBvc2l0X2FjY18xIiwidmFsdWUiOiIxMDAwMSIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJsb2FuX2FjY18xIiwidmFsdWUiOiIyMDAwMSIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJpbnZlc3RtZW50X2FjY18xIiwidmFsdWUiOiIzNzQ2MjcyIiwidHlwZSI6ImRlZmF1bHQiLCJlbmFibGVkIjp0cnVlfSx7ImtleSI6ImxvY19hY2NfMSIsInZhbHVlIjoiNTMyNjg4NCIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJ1c2VyXzEiLCJ2YWx1ZSI6ImZkeHVzZXIiLCJ0eXBlIjoiZGVmYXVsdCIsImVuYWJsZWQiOnRydWV9LHsia2V5IjoiY29uc2VudF9hdXRoX3Rva2VuIiwidmFsdWUiOiJ0ZXN0X2F1dGhfY29kZSIsInR5cGUiOiJkZWZhdWx0IiwiZW5hYmxlZCI6dHJ1ZX0seyJrZXkiOiJjb25zZW50SWQiLCJ2YWx1ZSI6IiIsInR5cGUiOiJhbnkiLCJlbmFibGVkIjp0cnVlfV0=)

1. Run the request from Postman: `Authorize (via PAR+RAR)`.
2. Copy the login URL from the `PAR Authorize` request curl section.
3. Paste the login URL in your browser to start the consent journey.
4. Log in with a valid user (fdxuser, fdxuser1, fdxuser2). After a successful login, you can select the accounts for which you want to give consent.
5. Once the account is selected, hit the submit button.
6. Upon successful consent, you'll receive a success message. Copy the authorization code (the "code" field) from the URL in the browser.
7. Paste the authorization code into the body of the Get Access Token request, under the "code" key.
8. Hit the `Get Access Token` request endpoint. You will get the authorization token.
9. Now you can access the resource API using this authorization token.

## License
This is an open-source project and does not have any specific licensing.

## Contact Information
For any queries, please post a comment on GitHub. We will look into it and get back to you.
